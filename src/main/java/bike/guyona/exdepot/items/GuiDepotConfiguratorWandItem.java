package bike.guyona.exdepot.items;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.network.configuredepot.ConfigureDepotResult;
import bike.guyona.exdepot.network.getdepot.GetDepotResponse;
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
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

import static bike.guyona.exdepot.ExDepotMod.NETWORK_INSTANCE;
import static bike.guyona.exdepot.capabilities.DepotCapabilityProvider.DEPOT_CAPABILITY;


public class GuiDepotConfiguratorWandItem extends DepotConfiguratorWandBase {
    public GuiDepotConfiguratorWandItem(Properties properties) {
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
            ExDepotMod.LOGGER.error("Impossible: GuiWand received a useOn event meant for something else.");
            return InteractionResult.FAIL;
        }
        Level level = ctx.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(ctx.getClickedPos());
        return handleGuiConfigure(level.isClientSide, player, blockEntity);
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!isWand(itemstack.getItem())) {
            ExDepotMod.LOGGER.error("Impossible: GuiWand received a use event meant for something else.");
            return new InteractionResultHolder<>(InteractionResult.FAIL, itemstack);
        }
        return new InteractionResultHolder<>(handleGuiConfigure(level.isClientSide, player, null), itemstack);
    }

    public static InteractionResult handleGuiConfigure(boolean isClientSide, Player player, BlockEntity target) {
        if (isClientSide) {
            return InteractionResult.CONSUME;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (target == null) {
            NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new GetDepotResponse(ConfigureDepotResult.NO_SELECTION, null,null));
            return InteractionResult.CONSUME;
        }
        LazyOptional<IDepotCapability> depotCap = target.getCapability(DEPOT_CAPABILITY, Direction.UP);
        if (!depotCap.isPresent()) {
            NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new GetDepotResponse(ConfigureDepotResult.WHAT_IS_THAT, null,null));
            return InteractionResult.CONSUME;
        }
        NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new GetDepotResponse(ConfigureDepotResult.SUCCESS, depotCap.orElse(null), target.getBlockPos()));
        return InteractionResult.SUCCESS;
    }
}
