package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.network.StorageConfigRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import static bike.guyona.exdepot.Ref.GEAR_SMALL_BIDX;


/**
 * Created by longb on 9/17/2017.
 */
public class StorageConfigButton extends GuiIconButton {
    public StorageConfigButton(int buttonId, int x, int y, int width, int height) {
        super(buttonId, x, y, width, height, Ref.NAME, GEAR_SMALL_BIDX);
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
