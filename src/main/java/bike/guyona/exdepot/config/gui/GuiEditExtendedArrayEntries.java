package bike.guyona.exdepot.config.gui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.GuiEditArray;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;

public class GuiEditExtendedArrayEntries extends GuiEditArrayEntries {
    public GuiEditExtendedArrayEntries(GuiEditArray parent, Minecraft mc, IConfigElement configElement, Object[] beforeValues, Object[] currentValues) {
        super(parent, mc, configElement, beforeValues, currentValues);
        listEntries.clear();
        for (Object value : currentValues)
            listEntries.add(new ClassnameEntry(this.owningGui, this, configElement, value.toString()));

        if (!configElement.isListLengthFixed())
            listEntries.add(new BaseEntry(this.owningGui, this, configElement));
    }

    public void addNewEntry(int index)
    {
        listEntries.add(index, new ClassnameEntry(this.owningGui, this, this.configElement, ""));
        this.canAddMoreEntries = !configElement.isListLengthFixed()
                && (configElement.getMaxListLength() == -1 || this.listEntries.size() - 1 < configElement.getMaxListLength());
        keyTyped((char) Keyboard.CHAR_NONE, Keyboard.KEY_END);
    }
}
