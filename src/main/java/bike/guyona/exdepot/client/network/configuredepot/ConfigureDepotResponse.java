package bike.guyona.exdepot.client.network.configuredepot;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.network.configuredepot.ConfigureDepotResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class ConfigureDepotResponse {
    public static void playConfigureDepotSound(ConfigureDepotResult configureDepotResult) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            ExDepotMod.LOGGER.error("Impossible: got an ingame-only network event while no player was loaded.");
            return;
        }
        Minecraft.getInstance().player.playSound(configureDepotResult.getSound(), 1,1);
    }
}
