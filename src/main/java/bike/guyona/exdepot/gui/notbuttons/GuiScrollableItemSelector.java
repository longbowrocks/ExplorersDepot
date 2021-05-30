package bike.guyona.exdepot.gui.notbuttons;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.gui.interfaces.IHasTooltip;
import bike.guyona.exdepot.sortingrules.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by longb on 12/7/2017.
 */
public class GuiScrollableItemSelector extends TextFieldWidget implements IHasTooltip {
    private int mainGuiWidth;
    private int mainGuiHeight;
    private List<AbstractSortingRule> searchResults;
    private ResultList resultListGui;
    private int maxListHeight;
    private FontRenderer privFontRenderer; // I could change the asm fontRendererInstance to public, but no thanks.
    private String longTooltip;
    private String longTooltipCache;

    private StorageConfigGui configHolder;

    public GuiScrollableItemSelector(FontRenderer fr, int x, int y, int width, int height,
                                     int maxHeight, int mainGuiWidth, int mainGuiHeight, StorageConfigGui configHolder) {
        super(fr, x, y, width, height, "");
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
        super.render(mouseX, mouseY, partialTicks);
        if (resultListGui != null) {
            resultListGui.render(mouseX, mouseY, partialTicks);
        }
    }

//    public void handleMouseInput() throws IOException {
//        Minecraft mc = Minecraft.getInstance();
//        Point mouse = MouseInfo.getPointerInfo().getLocation();
//        int mouseX = mouse.x * this.mainGuiWidth / mc.currentScreen.width;
//        int mouseY = this.mainGuiHeight - mouse.y * this.mainGuiHeight / mc.currentScreen.height - 1;
//        if (resultListGui != null) {
//            resultListGui.handleMouseInput(mouseX, mouseY);
//        }
//    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        boolean keyTyped =  super.charTyped(typedChar, keyCode);
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
        for (int i = ExDepotMod.sortingRuleProvider.ruleClasses.size()-1; i>=0; i--) {
            Class<? extends AbstractSortingRule> ruleClass = ExDepotMod.sortingRuleProvider.ruleClasses.get(i);
            for(AbstractSortingRule baseRule : ExDepotMod.sortingRuleProvider.getAllRules(ruleClass)) {
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
            longTooltipCache = new TranslationTextComponent(longTooltip,
                    TextFormatting.LIGHT_PURPLE,
                    TextFormatting.BLUE,
                    TextFormatting.GOLD,
                    TextFormatting.RESET).getUnformattedComponentText();
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
    private class ResultList extends OptionsRowList {

        public ResultList()
        {
            super(Minecraft.getInstance(),
                    GuiScrollableItemSelector.this.width,
                    GuiScrollableItemSelector.this.height,
                    GuiScrollableItemSelector.this.y + GuiScrollableItemSelector.this.height,
                    GuiScrollableItemSelector.this.y + GuiScrollableItemSelector.this.height +
                            Math.min(GuiScrollableItemSelector.this.maxListHeight,
                                     StorageConfigGui.BUTTON_HEIGHT *
                                            GuiScrollableItemSelector.this.searchResults.size()),
                    StorageConfigGui.BUTTON_HEIGHT);
        }

        boolean containsClick(int mouseX, int mouseY) {
            return mouseX > this.getLeft() && mouseX < this.getRight() &&
                    mouseY > this.getTop() && mouseY < this.getBottom();
        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            GuiScrollableItemSelector.this.searchResults.get(slotIdx).draw(
                    this.x0, slotTop, Ref.Z_LEVEL);
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {
            configHolder.addConfigItem(searchResults.get(index));
        }

        @Override protected int getItemCount() {
            return GuiScrollableItemSelector.this.searchResults.size();
        }
        @Override protected boolean isSelectedItem(int index) {
            return false;
        }
        @Override protected void renderBackground() {}
    }

    private class ResultItem extends OptionsRowList.Row {
        @Override
        public void render(int p_render_1_, int p_render_2_, int p_render_3_, int p_render_4_, int p_render_5_, int p_render_6_, int p_render_7_, boolean p_render_8_, float p_render_9_) {
            GuiScrollableItemSelector.this.searchResults.get(slotIdx).draw(
                    this.x0, slotTop, Ref.Z_LEVEL);
        }
    }
}
