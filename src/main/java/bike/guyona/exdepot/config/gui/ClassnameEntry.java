package bike.guyona.exdepot.config.gui;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.config.ExDepotConfig;
import bike.guyona.exdepot.proxy.ClientProxy;
import net.minecraftforge.fml.client.config.GuiEditArray;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

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
            String orderAccessed;
            if (textFieldValue.getText().contains("*")) {
                orderAccessed = "?";
            } else {
                int orderAccessedInt = ((ClientProxy) ExDepotMod.proxy).guiContainerAccessOrders.get(textFieldValue.getText());
                orderAccessed = Integer.toString(orderAccessedInt);
            }
            this.owningScreen.drawString(
                    this.owningEntryList.getMC().fontRenderer,
                    orderAccessed,
                    2,
                    y + 2,
                    2147483647);
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
        String matchOrName = textFieldValue.getText();
        isValidValue = false;
        for (String name : ((ClientProxy)ExDepotMod.proxy).guiContainerAccessOrders.keySet()) {
            if (name.equals(matchOrName) || ExDepotConfig.globMatch(matchOrName, name)) {
                isValidValue = true;
                return;
            }
        }
    }
}
