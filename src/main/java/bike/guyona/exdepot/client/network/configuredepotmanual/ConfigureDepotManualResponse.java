package bike.guyona.exdepot.client.network.configuredepotmanual;

import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.client.gui.DepotRulesScreen;
import net.minecraft.client.Minecraft;

import javax.annotation.ParametersAreNonnullByDefault;

public class ConfigureDepotManualResponse {
    @ParametersAreNonnullByDefault
    public static void openGui(IDepotCapability cap) {
        Minecraft.getInstance().setScreen(new DepotRulesScreen(null, cap));
    }
}
