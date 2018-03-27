package bike.guyona.exdepot.gui.notbuttons;

import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.gui.interfaces.IHasTooltip;
import bike.guyona.exdepot.sortingrules.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static bike.guyona.exdepot.ExDepotMod.proxy;


/**
 * Created by longb on 12/7/2017.
 */
public class GuiScrollableItemSelector extends GuiTextField implements IHasTooltip {
    private int mainGuiWidth;
    private int mainGuiHeight;
    private List<AbstractSortingRule> searchResults;
    private ResultList resultListGui;
    private int maxListHeight;
    private FontRenderer privFontRenderer; // I could change the asm fontRendererInstance to public, but no thanks.
    private String longTooltip;
    private String longTooltipCache;

    private StorageConfigGui configHolder;

    public GuiScrollableItemSelector(int componentId, FontRenderer fr, int x, int y, int width, int height,
                                     int maxHeight, int mainGuiWidth, int mainGuiHeight, StorageConfigGui configHolder) {
        super(componentId, fr, x, y, width, height);
        this.mainGuiWidth = mainGuiWidth;
        this.mainGuiHeight = mainGuiHeight;
        this.privFontRenderer = fr;
        this.maxListHeight = maxHeight;
        this.configHolder = configHolder;
        this.searchResults = new ArrayList<>();

        longTooltip = "exdepot.tooltip.searchbar.adv";
        longTooltipCache = null;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawTextBox();
        if (resultListGui != null) {
            resultListGui.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    public void handleMouseInput() throws IOException {
        Minecraft mc = Minecraft.getMinecraft();
        int mouseX = Mouse.getEventX() * this.mainGuiWidth / mc.displayWidth;
        int mouseY = this.mainGuiHeight - Mouse.getEventY() * this.mainGuiHeight / mc.displayHeight - 1;
        if (resultListGui != null) {
            resultListGui.handleMouseInput(mouseX, mouseY);
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        boolean keyTyped =  super.textboxKeyTyped(typedChar, keyCode);
        if (keyTyped) {
            updateSearchResults();
        }
        return keyTyped;
    }

    public void clearSearchResults() {
        searchResults.clear();
        resultListGui = null;
    }

    private void updateSearchResults() {
        searchResults.clear();
        for (int i=proxy.sortingRuleProvider.ruleClasses.size()-1; i>=0; i--) {
            Class<? extends AbstractSortingRule> ruleClass = proxy.sortingRuleProvider.ruleClasses.get(i);
            for(AbstractSortingRule baseRule : proxy.sortingRuleProvider.getAllRules(ruleClass)) {
                if (baseRule.getDisplayName().toLowerCase().startsWith(getText().toLowerCase())) {
                    searchResults.add(baseRule);
                }
            }
        }
        if (searchResults.size() > 0) {
            resultListGui = new ResultList();
        } else {
            resultListGui = null;
        }
    }

    @Override
    public String getTooltip() {
        return null;
    }

    @Override
    public String getLongTooltip() {
        if (longTooltipCache == null) {
            longTooltipCache = new TextComponentTranslation(longTooltip,
                    TextFormatting.LIGHT_PURPLE,
                    TextFormatting.BLUE,
                    TextFormatting.GOLD,
                    TextFormatting.RESET).getUnformattedText();
        }
        return longTooltipCache;
    }

    @Override
    public boolean containsClick(int mouseX, int mouseY) {
        boolean inTextField = mouseX > x && mouseX < x + width &&
                mouseY > y && mouseY < y + height;
        boolean inResultsList = resultListGui != null && resultListGui.containsClick(mouseX, mouseY);
        return inTextField || inResultsList;
    }
    private class ResultList extends GuiScrollingList {

        public ResultList()
        {
            super(Minecraft.getMinecraft(),
                    GuiScrollableItemSelector.this.width,
                    GuiScrollableItemSelector.this.height,
                    GuiScrollableItemSelector.this.y + GuiScrollableItemSelector.this.height,
                    GuiScrollableItemSelector.this.y + GuiScrollableItemSelector.this.height +
                            Math.min(GuiScrollableItemSelector.this.maxListHeight,
                                     StorageConfigGui.BUTTON_HEIGHT *
                                            GuiScrollableItemSelector.this.searchResults.size()),
                    GuiScrollableItemSelector.this.x,
                    StorageConfigGui.BUTTON_HEIGHT,
                    Minecraft.getMinecraft().displayWidth,
                    Minecraft.getMinecraft().displayHeight);
            this.setHeaderInfo(false, 0);
        }

        boolean containsClick(int mouseX, int mouseY) {
            return mouseX > left && mouseX < left + width &&
                    mouseY > top && mouseY < top + height;
        }

        @Override
        public void handleMouseInput(int mouseX, int mouseY) throws IOException {
            super.handleMouseInput(mouseX, mouseY);
        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            Minecraft mc = Minecraft.getMinecraft();
            GuiScrollableItemSelector.this.searchResults.get(slotIdx).draw(
                    GuiScrollableItemSelector.this.x, slotTop, GuiScrollableItemSelector.this.zLevel);
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {
            configHolder.addConfigItem(searchResults.get(index));
        }

        @Override protected int getSize() {
            return GuiScrollableItemSelector.this.searchResults.size();
        }
        @Override protected boolean isSelected(int index) {
            return false;
        }
        @Override protected void drawBackground() {}
    }
}
