package bike.guyona.exdepot.storageconfig.gui;

import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

/**
 * Created by longb on 12/6/2017.
 */
public class StorageConfigGui extends GuiScreen implements GuiSlider.FormatHelper, GuiPageButtonList.GuiResponder {
    private int buttonId;
    private GuiButton allItemsToggle;
    private GuiButton saveConfigButton;
    private GuiButton clearConfigButton;
    private GuiPageButtonList myList;

    public StorageConfigGui() {
        buttonId = 0;
    }

    public String getText(int id, String name, float value)
    {
        return name + ": " + Float.toString(value);
    }

    public void setEntryValue(int id, String value) {
    }

    public void setEntryValue(int id, boolean value) {
    }

    public void setEntryValue(int id, float value) {
    }

    public void initGui() {
        // Create my buttons
        allItemsToggle = new GuiButton(buttonId++, this.width / 2 - 200, 50, 50,20, "All Items");
        saveConfigButton = new GuiButton(buttonId++, this.width / 2 - 100, 50, 50,20, "Save");
        clearConfigButton = new GuiButton(buttonId++, this.width / 2, 50, 50,20, "Clear");
        GuiPageButtonList.GuiListEntry[] wtfWhySoLong = new GuiPageButtonList.GuiListEntry[] {
                new GuiPageButtonList.GuiSlideEntry(
                        160, I18n.format("createWorld.customize.custom.seaLevel", new Object[0]),
                        true, this, 1.0F, 255.0F, (float)64),
                new GuiPageButtonList.GuiButtonEntry(
                        148, I18n.format("createWorld.customize.custom.useCaves", new Object[0]),
                        true, false),
                new GuiPageButtonList.GuiButtonEntry(
                        150, I18n.format("createWorld.customize.custom.useStrongholds", new Object[0]),
                        true, false),
                new GuiPageButtonList.GuiButtonEntry(
                        151, I18n.format("createWorld.customize.custom.useVillages", new Object[0]),
                        true, false)
        };
        myList = new GuiPageButtonList(this.mc, this.width, this.height, 32, this.height - 32,
                25, this, new GuiPageButtonList.GuiListEntry[][] {wtfWhySoLong});
        // Add my buttons.
        buttonList.add(allItemsToggle);
        buttonList.add(saveConfigButton);
        buttonList.add(clearConfigButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        myList.drawScreen(mouseX,mouseY,partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
