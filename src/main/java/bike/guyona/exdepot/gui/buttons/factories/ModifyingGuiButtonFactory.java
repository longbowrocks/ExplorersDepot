package bike.guyona.exdepot.gui.buttons.factories;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.gui.buttons.IngameConfigButton;
import bike.guyona.exdepot.gui.buttons.StorageConfigButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;

import static bike.guyona.exdepot.Ref.INGAME_CONFIG_BUTTON_ID;
import static bike.guyona.exdepot.Ref.STORAGE_CONFIG_BUTTON_ID;

public class ModifyingGuiButtonFactory {
    public static GuiButton createButton(GuiContainer guiChest, int buttonId, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        switch (buttonId) {
            case Ref.STORAGE_CONFIG_BUTTON_ID:
                return new StorageConfigButton(STORAGE_CONFIG_BUTTON_ID, buttonX, buttonY, buttonWidth, buttonHeight);
            case Ref.INGAME_CONFIG_BUTTON_ID:
                return new IngameConfigButton(INGAME_CONFIG_BUTTON_ID, guiChest, buttonX, buttonY, buttonWidth, buttonHeight);
        }
        return null;
    }
}
