package bike.guyona.exdepot.storageconfig;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.storageconfig.capability.StorageConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.util.Vector;

/**
 * Created by longb on 9/17/2017.
 */
public class StorageConfigButton extends GuiButton {
    public StorageConfigButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int i, int j) {
        if (super.mousePressed(mc, i, j)) {
            StorageConfig data = new StorageConfig(true, new Vector<>(), new Vector<>());
            ExDepotMod.NETWORK.sendToServer(new StorageConfigMessage(data));
            return true;
        }else {
            return false;
        }
    }
}
