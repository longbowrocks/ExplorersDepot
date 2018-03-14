package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.Ref.*;

public class UseNbtButton extends GuiIconButton {
    public UseNbtButton(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, "Toggle Use NBT", NBT_YES_BIDX);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int i, int j) {
        if (super.mousePressed(mc, i, j)) {
            if(mc.world != null && mc.player != null) {
                if(mc.currentScreen != null && mc.currentScreen instanceof StorageConfigGui) {
                    StorageConfigGui confGui = (StorageConfigGui) mc.currentScreen;
                    StorageConfig conf = confGui.getStorageConfig();
                    conf.setUseNbt(!conf.getUseNbt());
                    confGui.setStorageConfig(conf);
                    setToggle(conf.getUseNbt());
                } else {
                    LOGGER.error("usenbt screen is "+(mc.currentScreen == null ? "NULL" : mc.currentScreen.toString()));
                }
            }
            return true;
        }else {
            return false;
        }
    }

    public void setToggle(boolean value) {
        buttonIndex = value ? NBT_YES_BIDX : NBT_NO_BIDX;
    }
}
