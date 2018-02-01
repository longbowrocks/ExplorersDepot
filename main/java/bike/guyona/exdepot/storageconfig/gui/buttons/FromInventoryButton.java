package bike.guyona.exdepot.storageconfig.gui.buttons;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.storageconfig.StorageConfigCreateFromChestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class FromInventoryButton extends GuiButton{
    public FromInventoryButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int i, int j) {
        if (super.mousePressed(mc, i, j)) {
            // TODO: send message to server that tells it to use player.openContainer.inventory to create a StorageConfig, and re-init gui from that.
            ExDepotMod.NETWORK.sendToServer(new StorageConfigCreateFromChestMessage());
            return true;
        }else {
            return false;
        }
    }
}
