package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.Ref.CHECKBOX_NO_ASTERISK_BIDX;
import static bike.guyona.exdepot.Ref.CHECKBOX_YES_ASTERISK_BIDX;

public class AllItemsButton extends GuiIconButton {
    public AllItemsButton(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, "Toggle Accept Everything", CHECKBOX_YES_ASTERISK_BIDX);
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
                    setToggle(conf.allItems);
                } else {
                    LOGGER.error("allitems screen is "+(mc.currentScreen == null ? "NULL" : mc.currentScreen.toString()));
                }
            }
            return true;
        }else {
            return false;
        }
    }

    public void setToggle(boolean value) {
        buttonIndex = value ? CHECKBOX_YES_ASTERISK_BIDX : CHECKBOX_NO_ASTERISK_BIDX;
    }
}