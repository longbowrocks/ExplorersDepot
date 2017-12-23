package bike.guyona.exdepot.storageconfig.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;

import java.io.IOException;

/**
 * Created by longb on 12/6/2017.
 */
public class StorageConfigGui extends GuiScreen {
    private static int MIN_ELEMENT_SEPARATION = 10;
    private static int BUTTON_HEIGHT = 20;
    private int buttonId;
    private GuiTextField searchField;
    private GuiButton allItemsToggle;
    private GuiButton ezConfigButton;
    private GuiButton saveConfigButton;
    private GuiButton clearConfigButton;

    public StorageConfigGui() {
        buttonId = 0;
    }

    public void initGui() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int xOffset = 0;
        int prevItemWidth = 0;
        searchField = new GuiTextField(buttonId++, fr,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=200, BUTTON_HEIGHT);
        // Create my buttons
        int firstButtonOffset = xOffset + prevItemWidth;
        allItemsToggle = new GuiButton(buttonId++,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=50, BUTTON_HEIGHT, "All Items");
        ezConfigButton = new GuiButton(buttonId++,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=100, BUTTON_HEIGHT, "Set From Contents");
        prevItemWidth = 0;
        xOffset = firstButtonOffset;
        saveConfigButton = new GuiButton(buttonId++,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION*2+BUTTON_HEIGHT, prevItemWidth=50, BUTTON_HEIGHT, "Save");
        clearConfigButton = new GuiButton(buttonId++,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION*2+BUTTON_HEIGHT, prevItemWidth=50, BUTTON_HEIGHT, "Clear");
        // Add my buttons.
        buttonList.add(allItemsToggle);
        buttonList.add(ezConfigButton);
        buttonList.add(saveConfigButton);
        buttonList.add(clearConfigButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        searchField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        searchField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
