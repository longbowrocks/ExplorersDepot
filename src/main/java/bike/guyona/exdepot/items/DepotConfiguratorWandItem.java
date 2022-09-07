package bike.guyona.exdepot.items;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.client.gui.DepotRulesScreen;
import bike.guyona.exdepot.events.EventHandler;
import bike.guyona.exdepot.network.configuredepot.ConfigureDepotResponse;
import bike.guyona.exdepot.network.configuredepot.ConfigureDepotResult;
import bike.guyona.exdepot.sortingrules.SortingRuleProvider;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;


import javax.annotation.ParametersAreNonnullByDefault;

import static bike.guyona.exdepot.ExDepotMod.NETWORK_INSTANCE;
import static bike.guyona.exdepot.ExDepotMod.WAND_ITEM;
import static bike.guyona.exdepot.capabilities.DepotCapabilityProvider.DEPOT_CAPABILITY;

public class DepotConfiguratorWandItem extends Item {
    public static final String WAND_MODE_KEY = String.format("%s:wand_mode", Ref.MODID);

    public DepotConfiguratorWandItem(Properties properties) {
        super(properties.stacksTo(1).tab(CreativeModeTab.TAB_TOOLS));
    }

    /**
     * Interesting notes:
     * 1. useOn() precedes use() in a tick.
     * 2. They are mutually exclusive as long as useOn() does not return InteractionResult.PASS
     */
    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null) {
            ExDepotMod.LOGGER.error("Explorer's Depot wand was used by a non-player? No dice.");
            return InteractionResult.FAIL;
        }
        ItemStack itemstack = ctx.getItemInHand();
        if (!WAND_ITEM.get().equals(itemstack.getItem())) {
            ExDepotMod.LOGGER.error("Impossible: a wand was used on something, but the used item was not a wand.");
            return InteractionResult.FAIL;
        }
        Mode mode = getMode(itemstack);
        Level level = ctx.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(ctx.getClickedPos());
        return switch (mode) {
            case AUTO_CONFIGURE -> this.handleAutoConfigure(level.isClientSide, level, player, blockEntity);
            case GUI_CONFIGURE -> this.handleGuiConfigure(level.isClientSide);
            default -> InteractionResult.CONSUME;
        };
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!WAND_ITEM.get().equals(itemstack.getItem())) {
            ExDepotMod.LOGGER.error("Impossible: a wand was used, but the used item was not a wand.");
            return new InteractionResultHolder<>(InteractionResult.FAIL, itemstack);
        }
        Mode mode = getMode(itemstack);
        return switch (mode) {
            case AUTO_CONFIGURE -> new InteractionResultHolder<>(this.handleAutoConfigure(level.isClientSide, level, player, null), itemstack);
            case GUI_CONFIGURE -> new InteractionResultHolder<>(this.handleGuiConfigure(level.isClientSide), itemstack);
            default -> new InteractionResultHolder<>(InteractionResult.CONSUME, itemstack);
        };
    }

    public static @NotNull Mode getMode(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains(WAND_MODE_KEY, Tag.TAG_INT)) {
            return Mode.values()[nbt.getInt(WAND_MODE_KEY)];
        }
        return Mode.AUTO_CONFIGURE;
    }

    // Must run on server.
    public static void setMode(ItemStack stack, Mode mode) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putInt(WAND_MODE_KEY, mode.ordinal());
    }

    private InteractionResult handleGuiConfigure(boolean isClientSide) {
        if (!isClientSide) {
            return InteractionResult.CONSUME;
        }
        Minecraft.getInstance().setScreen(new DepotRulesScreen(null));
        return InteractionResult.SUCCESS;
    }

    // Remember to only add capabilities on the server, as that's where they're persisted.
    private InteractionResult handleAutoConfigure(boolean isClientSide, Level level, Player player, BlockEntity blockEntity) {
        if (isClientSide) {
            return InteractionResult.CONSUME;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (blockEntity == null) {
            ExDepotMod.LOGGER.info("No selection");
            NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ConfigureDepotResponse(ConfigureDepotResult.NO_SELECTION));
            return InteractionResult.CONSUME;
        }
        LazyOptional<IDepotCapability> depotCap = blockEntity.getCapability(DEPOT_CAPABILITY, Direction.UP);
        if (!depotCap.isPresent()) {
            NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ConfigureDepotResponse(ConfigureDepotResult.WHAT_IS_THAT));
            return InteractionResult.CONSUME;
        }
        this.addModSortingRules(depotCap.orElse(null), blockEntity);
        EventHandler.VIEW_DEPOTS_CACHE_WHISPERER.triggerUpdateFromServer(level, player.blockPosition());
        NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ConfigureDepotResponse(ConfigureDepotResult.SUCCESS));
        return InteractionResult.SUCCESS;
    }

    private void addModSortingRules(IDepotCapability depotCapability, BlockEntity depot) {
        LazyOptional<IItemHandler> lazyItemHandler = depot.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
        if (!lazyItemHandler.isPresent()) {
            ExDepotMod.LOGGER.error("Impossible: {} has Depot capability but not ItemHandler capability.", depot);
            return;
        }
        SortingRuleProvider ruleProvider = new SortingRuleProvider();
        IItemHandler itemHandler = lazyItemHandler.orElse(null);
        for (int i=0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            ModSortingRule rule = ruleProvider.getRule(stack, ModSortingRule.class);
            depotCapability.addRule(rule);
        }
        depot.setChanged();
    }

    public enum Mode {
        AUTO_CONFIGURE,
        GUI_CONFIGURE
    }
}
