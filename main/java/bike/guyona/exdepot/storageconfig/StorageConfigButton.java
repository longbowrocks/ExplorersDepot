package bike.guyona.exdepot.storageconfig;

import bike.guyona.exdepot.ExDepotMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;


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
            ExDepotMod.NETWORK.sendToServer(new StorageConfigRequestMessage());
            return true;
        }else {
            return false;
        }
    }
}
