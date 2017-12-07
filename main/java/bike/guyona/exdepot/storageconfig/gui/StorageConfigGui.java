package bike.guyona.exdepot.storageconfig.gui;

import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

/**
 * Created by longb on 12/6/2017.
 */
public class StorageConfigGui extends GuiScreen{
    private int buttonId;
    private GuiButton allItemsToggle;
    private GuiButton saveConfigButton;
    private GuiButton clearConfigButton;
    private GuiPageButtonList myList;

    public StorageConfigGui() {
        buttonId = 0;
    }

    public void initGui() {
        // Create my buttons
        allItemsToggle = new GuiButton(buttonId++, this.width / 2 - 200, 50, 50,20, "All Items");
        saveConfigButton = new GuiButton(buttonId++, this.width / 2 - 100, 50, 50,20, "Save");
        clearConfigButton = new GuiButton(buttonId++, this.width / 2, 50, 50,20, "Clear");
        // Add my buttons.
        buttonList.add(allItemsToggle);
        buttonList.add(saveConfigButton);
        buttonList.add(clearConfigButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        //myList.drawScreen(mouseX,mouseY,partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
