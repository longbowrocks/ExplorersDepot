package bike.guyona.exdepot.client.network.wandmodechanged;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.sounds.SoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class ChangeWandModeResponse {
    public static void playWandModeSwitchSound(){
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            ExDepotMod.LOGGER.error("Impossible: the client doesn't have a player");
            return;
        }
        Minecraft.getInstance().player.playSound(SoundEvents.WAND_SWITCH.get(), 1, 1);
    }
}
