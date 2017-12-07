package bike.guyona.exdepot.storageconfig.gui;

import net.minecraft.client.gui.GuiScreen;

/**
 * Created by longb on 12/6/2017.
 */
public class StorageConfigGui extends GuiScreen {
    public void initGui() {
        //this.buttonList.add(this.a = new GuiButton(0, this.width / 2 - 100, this.height / 2 - 24, "This is button a"));
        //this.buttonList.add(this.b = new GuiButton(1, this.width / 2 - 100, this.height / 2 + 4, "This is button b"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
