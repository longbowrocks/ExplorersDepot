package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.network.StorageConfigCreateFromChestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import static bike.guyona.exdepot.Ref.CHEST_AND_GEAR_BIDX;

public class FromInventoryButton extends GuiIconButton{
    public FromInventoryButton(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, "Set config from contents", "", CHEST_AND_GEAR_BIDX);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int i, int j) {
        if (super.mousePressed(mc, i, j)) {
            ExDepotMod.NETWORK.sendToServer(new StorageConfigCreateFromChestMessage());
            return true;
        }else {
            return false;
        }
    }
}
