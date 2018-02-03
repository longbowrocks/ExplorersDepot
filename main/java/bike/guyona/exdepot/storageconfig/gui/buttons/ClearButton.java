package bike.guyona.exdepot.storageconfig.gui.buttons;

import bike.guyona.exdepot.storageconfig.capability.StorageConfig;
import bike.guyona.exdepot.storageconfig.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

public class ClearButton extends GuiButton {
    public ClearButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int i, int j) {
        if (super.mousePressed(mc, i, j)) {
            if(mc.world != null && mc.player != null) {
                if(mc.currentScreen != null && mc.currentScreen instanceof StorageConfigGui) {
                    StorageConfigGui confGui = (StorageConfigGui) mc.currentScreen;
                    confGui.setStorageConfig(new StorageConfig());
                } else {
                    LOGGER.error("clear screen is "+(mc.currentScreen == null ? "NULL" : mc.currentScreen.toString()));
                }
            }
            return true;
        }else {
            return false;
        }
    }
}
