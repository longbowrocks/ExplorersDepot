package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

public class AllItemsButton extends GuiButton {
    public AllItemsButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    public AllItemsButton(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int i, int j) {
        if (super.mousePressed(mc, i, j)) {
            if(mc.world != null && mc.player != null) {
                if(mc.currentScreen != null && mc.currentScreen instanceof StorageConfigGui) {
                    StorageConfigGui confGui = (StorageConfigGui) mc.currentScreen;
                    StorageConfig conf = confGui.getStorageConfig();
                    conf.allItems = !conf.allItems;
                    confGui.setStorageConfig(conf);
                    displayString = "All Items: "+(conf.allItems ? "ON":"OFF");
                } else {
                    LOGGER.error("allitems screen is "+(mc.currentScreen == null ? "NULL" : mc.currentScreen.toString()));
                }
            }
            return true;
        }else {
            return false;
        }
    }
}
