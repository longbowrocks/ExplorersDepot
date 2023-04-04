package bike.guyona.exdepot.client.network.configuredepotmanual;

import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.client.gui.DepotRulesScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import javax.annotation.ParametersAreNonnullByDefault;

public class ConfigureDepotManualResponse {
    @ParametersAreNonnullByDefault
    public static void openGui(IDepotCapability cap, BlockPos loc) {
        Minecraft.getInstance().setScreen(new DepotRulesScreen(null, cap, loc));
    }
}
