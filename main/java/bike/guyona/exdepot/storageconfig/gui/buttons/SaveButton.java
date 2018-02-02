package bike.guyona.exdepot.storageconfig.gui.buttons;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.storageconfig.StorageConfigCreateMessage;
import bike.guyona.exdepot.storageconfig.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

public class SaveButton extends GuiButton {
    public SaveButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    public SaveButton(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int i, int j) {
        if (super.mousePressed(mc, i, j)) {
            if(mc.world != null && mc.player != null) {
                if(mc.currentScreen != null && mc.currentScreen instanceof StorageConfigGui) {
                    StorageConfigGui confGui = (StorageConfigGui) mc.currentScreen;
                    ExDepotMod.NETWORK.sendToServer(new StorageConfigCreateMessage(confGui.getStorageConfig()));
                } else {
                    LOGGER.error("save screen is "+(mc.currentScreen == null ? "NULL" : mc.currentScreen.toString()));
                }
            }
            return true;
        }else {
            return false;
        }
    }
}
