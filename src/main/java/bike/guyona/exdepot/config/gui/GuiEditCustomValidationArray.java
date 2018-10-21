package bike.guyona.exdepot.config.gui;

import bike.guyona.exdepot.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.*;
import scala.actors.threadpool.Arrays;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static bike.guyona.exdepot.ExDepotMod.proxy;

public class GuiEditCustomValidationArray extends GuiEditArray{
    GuiButton populateButton;

    public GuiEditCustomValidationArray(GuiScreen parentScreen, IConfigElement configElement, int slotIndex, Object[] currentValues, boolean enabled) {
        super(parentScreen, configElement, slotIndex, currentValues, enabled);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.entryList = new GuiEditCustomValidationArrayEntries(this, this.mc, this.configElement, this.beforeValues, this.currentValues);
        this.populateButton = new GuiButtonExt(697348386, 2, 2, 60, 20, I18n.format("exdepot.config.compatListReset")) {
            @Override
            public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                if (super.mousePressed(mc, mouseX, mouseY)) {
                    List<String> alphabeticalClassnames = new LinkedList<>();
                    alphabeticalClassnames.addAll(((ClientProxy)proxy).guiContainerAccessOrders.keySet());
                    Collections.sort(alphabeticalClassnames);
                    while (entryList.listEntries.size() > 1) {entryList.removeEntry(0);}
                    int entryIdx = 0;
                    for (String entry : alphabeticalClassnames) {
                        ((GuiEditCustomValidationArrayEntries)entryList).addNewEntry(entryIdx++, entry);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        };
        this.buttonList.add(this.populateButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (this.populateButton.isMouseOver()) {
            this.drawToolTip(Arrays.asList(I18n.format("exdepot.config.compatListReset.tooltip").split("\n")), mouseX, mouseY);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);
        if (button.id == 2001)
        {
            this.entryList = new GuiEditCustomValidationArrayEntries(this, this.mc, this.configElement, this.beforeValues, this.currentValues);
        }
        else if (button.id == 2002)
        {
            this.entryList = new GuiEditCustomValidationArrayEntries(this, this.mc, this.configElement, this.beforeValues, this.currentValues);
        }
    }
}
