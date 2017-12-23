package bike.guyona.exdepot.storageconfig.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;


/**
 * Created by longb on 12/7/2017.
 */
public class GuiScrollableClickableItemSelector extends GuiTextField {
    public GuiScrollableClickableItemSelector(int componentId, FontRenderer fr, int x, int y, int width, int height, int maxHeight) {
        super(componentId, fr, x, y, width, height);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        boolean keyTyped =  super.textboxKeyTyped(typedChar, keyCode);
        if (keyTyped) {
            updateSearchResults();
        }
        return keyTyped;
    }
}
