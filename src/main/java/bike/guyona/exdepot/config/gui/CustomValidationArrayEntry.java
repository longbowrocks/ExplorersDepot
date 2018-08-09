package bike.guyona.exdepot.config.gui;

import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

public class CustomValidationArrayEntry extends GuiConfigEntries.ArrayEntry {
    public CustomValidationArrayEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement);
    }

    @Override
    public void updateValueButtonText()
    {
        this.btnValue.displayString = new TextComponentTranslation(configElement.getLanguageKey()).getUnformattedText();
    }

    @Override
    public void valueButtonPressed(int slotIndex)
    {
        mc.displayGuiScreen(new GuiEditCustomValidationArray(this.owningScreen, configElement, slotIndex, currentValues, enabled()));
    }
}
