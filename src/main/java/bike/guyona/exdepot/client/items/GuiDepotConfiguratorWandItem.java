package bike.guyona.exdepot.client.items;

import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.client.gui.DepotRulesScreen;
import bike.guyona.exdepot.network.configuredepot.ConfigureDepotResult;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

import static bike.guyona.exdepot.capabilities.DepotCapabilityProvider.DEPOT_CAPABILITY;

public class GuiDepotConfiguratorWandItem {
    public static InteractionResult handleGuiConfigure(boolean isClientSide, Player player, BlockEntity target) {
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
        Minecraft.getInstance().setScreen(new DepotRulesScreen(null, depotCap.orElse(null)));
        player.playSound(ConfigureDepotResult.SUCCESS.getSound(), 1,1);
        return InteractionResult.SUCCESS;
    }
}
