package bike.guyona.exdepot.storageconfig.gui.buttons;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.storageconfig.StorageConfigCreateFromChestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class FromInventoryButton extends GuiButton{
    public FromInventoryButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    public FromInventoryButton(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
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
