package bike.guyona.exdepot.items;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.helpers.ModSupportHelpers;
import bike.guyona.exdepot.network.configuredepot.ConfigureDepotResponse;
import bike.guyona.exdepot.network.configuredepot.ConfigureDepotResult;
import bike.guyona.exdepot.network.viewdepots.ViewDepotsCacheWhisperer;
import bike.guyona.exdepot.sortingrules.SortingRuleProvider;
import bike.guyona.exdepot.sortingrules.item.ItemSortingRule;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
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
import static bike.guyona.exdepot.capabilities.DepotCapabilityProvider.DEPOT_CAPABILITY;

public class AutoDepotConfiguratorWandItem extends DepotConfiguratorWandBase {
    public AutoDepotConfiguratorWandItem(Properties properties) {
        super(properties);
    }

    /**
     * Interesting notes:
     * 1. useOn() precedes use() in a tick.
     * 2. They are mutually exclusive unless useOn() returns InteractionResult.PASS
     */
    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null) {
            ExDepotMod.LOGGER.error("Impossible: wand was used by a non-player.");
            return InteractionResult.FAIL;
        }
        ItemStack itemstack = ctx.getItemInHand();
        if (!isWand(itemstack.getItem())) {
            ExDepotMod.LOGGER.error("Impossible: AutoWand received a useOn event meant for something else.");
            return InteractionResult.FAIL;
        }
        Level level = ctx.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(ctx.getClickedPos());
        return this.handleAutoConfigure(level.isClientSide, level, player, blockEntity);
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!isWand(itemstack.getItem())) {
            ExDepotMod.LOGGER.error("Impossible: AutoWand received a use event meant for something else.");
            return new InteractionResultHolder<>(InteractionResult.FAIL, itemstack);
        }
        return new InteractionResultHolder<>(this.handleAutoConfigure(level.isClientSide, level, player, null), itemstack);
    }

    // Remember to only add capabilities on the server, as that's where they're persisted.
    private InteractionResult handleAutoConfigure(boolean isClientSide, Level level, Player player, BlockEntity target) {
        if (isClientSide) {
            return InteractionResult.CONSUME;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (target == null) {
            NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ConfigureDepotResponse(ConfigureDepotResult.NO_SELECTION));
            return InteractionResult.CONSUME;
        }
        LazyOptional<IDepotCapability> depotCap = target.getCapability(DEPOT_CAPABILITY, Direction.UP);
        if (!depotCap.isPresent()) {
            NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ConfigureDepotResponse(ConfigureDepotResult.WHAT_IS_THAT));
            return InteractionResult.CONSUME;
        }
        this.addItemSortingRules(depotCap.orElse(null), target);
        for (BlockEntity e : ModSupportHelpers.getBigDepot(target)) {
            if (e != target) {
                e.getCapability(DEPOT_CAPABILITY, Direction.UP).ifPresent(cap -> cap.copyFrom(depotCap.orElse(null)));
            }
        }
        ViewDepotsCacheWhisperer.triggerUpdateFromServer(level, player.blockPosition());
        NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ConfigureDepotResponse(ConfigureDepotResult.SUCCESS));
        return InteractionResult.SUCCESS;
    }

    /**
     * Adds ItemSortingRules for every item in the depot that doesn't already have a rule.
     * @param depotCapability
     * @param depot
     */
    private void addItemSortingRules(IDepotCapability depotCapability, BlockEntity depot) {
        SortingRuleProvider ruleProvider = new SortingRuleProvider();
        for (BlockEntity blockEntity : ModSupportHelpers.getBigDepot(depot)) {
            LazyOptional<IItemHandler> lazyItemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
            if (!lazyItemHandler.isPresent()) {
                ExDepotMod.LOGGER.error("Impossible: {} has Depot capability but not ItemHandler capability.", blockEntity);
                return;
            }
            IItemHandler itemHandler = lazyItemHandler.orElse(null);
            for (int i=0; i < itemHandler.getSlots(); i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (stack.isEmpty()) {
                    continue;
                }
                ItemSortingRule rule = ruleProvider.getRule(stack, ItemSortingRule.class);
                depotCapability.addRule(rule);
            }
        }
        depot.setChanged();
    }
}
