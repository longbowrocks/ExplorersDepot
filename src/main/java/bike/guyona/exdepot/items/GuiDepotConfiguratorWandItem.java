package bike.guyona.exdepot.items;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.client.gui.DepotRulesScreen;
import bike.guyona.exdepot.network.configuredepot.ConfigureDepotResult;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

import static bike.guyona.exdepot.capabilities.DepotCapabilityProvider.DEPOT_CAPABILITY;

public class GuiDepotConfiguratorWandItem extends DepotConfiguratorWandBase {
    public GuiDepotConfiguratorWandItem(Properties properties) {
        super(properties);
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
        return this.handleGuiConfigure(level.isClientSide, player, blockEntity);
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!isWand(itemstack.getItem())) {
            ExDepotMod.LOGGER.error("Impossible: GuiWand received a use event meant for something else.");
            return new InteractionResultHolder<>(InteractionResult.FAIL, itemstack);
        }
        return new InteractionResultHolder<>(this.handleGuiConfigure(level.isClientSide, player, null), itemstack);
    }

    private InteractionResult handleGuiConfigure(boolean isClientSide, Player player, BlockEntity target) {
        if (!isClientSide) {
            return InteractionResult.CONSUME;
        }
        if (target == null) {
            player.playSound(ConfigureDepotResult.NO_SELECTION.getSound(), 1,1);
            return InteractionResult.CONSUME;
        }
        LazyOptional<IDepotCapability> depotCap = target.getCapability(DEPOT_CAPABILITY, Direction.UP);
        if (!depotCap.isPresent()) {
            player.playSound(ConfigureDepotResult.WHAT_IS_THAT.getSound(), 1,1);
            return InteractionResult.CONSUME;
        }
        Minecraft.getInstance().setScreen(new DepotRulesScreen(null));
        player.playSound(ConfigureDepotResult.SUCCESS.getSound(), 1,1);
        return InteractionResult.SUCCESS;
    }
}
