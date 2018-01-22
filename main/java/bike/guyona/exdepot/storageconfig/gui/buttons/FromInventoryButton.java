package bike.guyona.exdepot.storageconfig.gui.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class FromInventoryButton extends GuiButton{
    public FromInventoryButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int i, int j) {
        if (super.mousePressed(mc, i, j)) {
            return true;
        }else {
            return false;
        }
    }
}
