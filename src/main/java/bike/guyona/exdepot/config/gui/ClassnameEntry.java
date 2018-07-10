package bike.guyona.exdepot.config.gui;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.proxy.ClientProxy;
import net.minecraftforge.fml.client.config.GuiEditArray;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

import static net.minecraftforge.fml.client.config.GuiUtils.VALID;

public class ClassnameEntry extends GuiEditArrayEntries.StringEntry {
    public ClassnameEntry(GuiEditArray owningScreen, GuiEditArrayEntries owningEntryList, IConfigElement configElement, Object value)
    {
        super(owningScreen, owningEntryList, configElement, value);
        this.isValidated = true;

        this.validate();
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        if (isValidValue) {
            int orderAccessed = ((ClientProxy)ExDepotMod.proxy).guiContainerAccessOrders.get(textFieldValue.getText());
            if (orderAccessed > 0) {
                this.owningScreen.drawString(
                        this.owningEntryList.getMC().fontRenderer,
                        Integer.toString(orderAccessed),
                        2,
                        y + 2,
                        2147483647);
            }
        }
        super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected);
    }

    @Override
    public void keyTyped(char eventChar, int eventKey)
    {
        super.keyTyped(eventChar, eventKey);
        this.validate();
    }

    private void validate() {
        isValidValue = ((ClientProxy)ExDepotMod.proxy).guiContainerAccessOrders.containsKey(textFieldValue.getText());
    }

    private int getLeftMostUndrawnPixel(int listWidth) {
        return listWidth / 4 - owningEntryList.getMC().fontRenderer.getStringWidth(VALID) - 2;
    }
}
