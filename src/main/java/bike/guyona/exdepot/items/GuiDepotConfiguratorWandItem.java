package bike.guyona.exdepot.items;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.client.gui.DepotRulesScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

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
            ExDepotMod.LOGGER.error("Explorer's Depot wand was used by a non-player? No dice.");
            return InteractionResult.FAIL;
        }
        ItemStack itemstack = ctx.getItemInHand();
        if (!isWand(itemstack.getItem())) {
            ExDepotMod.LOGGER.error("Impossible: a wand was used on something, but the used item was not a wand.");
            return InteractionResult.FAIL;
        }
        Level level = ctx.getLevel();
        return this.handleGuiConfigure(level.isClientSide);
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!isWand(itemstack.getItem())) {
            ExDepotMod.LOGGER.error("Impossible: a wand was used, but the used item was not a wand.");
            return new InteractionResultHolder<>(InteractionResult.FAIL, itemstack);
        }
        return new InteractionResultHolder<>(this.handleGuiConfigure(level.isClientSide), itemstack);
    }

    private InteractionResult handleGuiConfigure(boolean isClientSide) {
        if (!isClientSide) {
            return InteractionResult.CONSUME;
        }
        Minecraft.getInstance().setScreen(new DepotRulesScreen(null));
        return InteractionResult.SUCCESS;
    }
}
