package bike.guyona.exdepot.gui;

import bike.guyona.exdepot.helpers.GuiHelpers;
import bike.guyona.exdepot.helpers.TrackableModCategoryPair;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.ModSortingRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;
import static bike.guyona.exdepot.helpers.ItemLookupHelpers.getSubtypes;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.DISALLOWED_CATEGORIES;


/**
 * Created by longb on 12/7/2017.
 */
public class GuiScrollableItemSelector extends GuiTextField {
    private int mainGuiWidth;
    private int mainGuiHeight;
    private List<Object> searchResults;
    private ResultList resultListGui;
    private int maxListHeight;
    private FontRenderer privFontRenderer; // I could change the asm fontRendererInstance to public, but no thanks.

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
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawTextBox();
        if (resultListGui != null) {
            resultListGui.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    public boolean containsClick(int mouseX, int mouseY) {
        boolean inTextField = mouseX > xPosition && mouseX < xPosition + width &&
                mouseY > yPosition && mouseY < yPosition + height;
        boolean inResultsList = resultListGui != null && resultListGui.containsClick(mouseX, mouseY);
        return inTextField || inResultsList;
    }

    public void handleMouseInput() throws IOException {
        Minecraft mc = Minecraft.getMinecraft();
        int mouseX = Mouse.getEventX() * this.mainGuiWidth / mc.displayWidth;
        int mouseY = this.mainGuiHeight - Mouse.getEventY() * this.mainGuiHeight / mc.displayHeight - 1;
        if (resultListGui != null) {
            resultListGui.handleMouseInput(mouseX, mouseY);
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
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
        for(AbstractSortingRule modBaseRule : proxy.sortingRuleProvider.getAllRules(ModSortingRule.class)) {
            ModSortingRule modRule = (ModSortingRule)modBaseRule;
            if (modRule.getDisplayName().toLowerCase().startsWith(getText().toLowerCase())) {
                searchResults.add(modRule);
            }
        }
        for(ModContainer mod : Loader.instance().getModList()) {
            if (mod.getName().toLowerCase().startsWith(getText().toLowerCase())) {
                for (CreativeTabs tab:CreativeTabs.CREATIVE_TAB_ARRAY) {
                    if (Arrays.asList(DISALLOWED_CATEGORIES).contains(tab)) {
                        continue;
                    }
                    searchResults.add(new TrackableModCategoryPair(mod, tab));
                }
            }
        }
        for(Item item : Item.REGISTRY) {
            for (ItemStack itemStack : getSubtypes(item)) {
                if (itemStack.getDisplayName().toLowerCase().contains(getText().toLowerCase())) {
                    searchResults.add(itemStack);
                }
            }
        }
        for (CreativeTabs tab:CreativeTabs.CREATIVE_TAB_ARRAY) {
            if (!Arrays.asList(DISALLOWED_CATEGORIES).contains(tab) &&
                    I18n.format(tab.getTranslatedTabLabel()).toLowerCase().startsWith(getText().toLowerCase())) {
                searchResults.add(tab);
            }
        }
        if (searchResults.size() > 0) {
            resultListGui = new ResultList();
        } else {
            resultListGui = null;
        }
    }

    private class ResultList extends GuiScrollingList {
        public ResultList()
        {
            super(Minecraft.getMinecraft(),
                    GuiScrollableItemSelector.this.width,
                    GuiScrollableItemSelector.this.height,
                    GuiScrollableItemSelector.this.yPosition + GuiScrollableItemSelector.this.height,
                    GuiScrollableItemSelector.this.yPosition + GuiScrollableItemSelector.this.height +
                            Math.min(GuiScrollableItemSelector.this.maxListHeight,
                                     StorageConfigGui.BUTTON_HEIGHT *
                                            GuiScrollableItemSelector.this.searchResults.size()),
                    GuiScrollableItemSelector.this.xPosition,
                    StorageConfigGui.BUTTON_HEIGHT,
                    Minecraft.getMinecraft().displayWidth,
                    Minecraft.getMinecraft().displayHeight);

            this.setHeaderInfo(false, 0);
        }

        public boolean containsClick(int mouseX, int mouseY) {
            return mouseX > left && mouseX < left + width &&
                    mouseY > top && mouseY < bottom;
        }

        @Override
        public void handleMouseInput(int mouseX, int mouseY) throws IOException {
            super.handleMouseInput(mouseX, mouseY);
        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            Minecraft mc = Minecraft.getMinecraft();
            if (GuiScrollableItemSelector.this.searchResults.get(slotIdx) instanceof ItemStack) {
                ItemStack item = (ItemStack)GuiScrollableItemSelector.this.searchResults.get(slotIdx);
                GuiHelpers.drawItem(GuiScrollableItemSelector.this.xPosition,
                        slotTop, item, GuiScrollableItemSelector.this.privFontRenderer);
                GuiScrollableItemSelector.this.privFontRenderer.drawString(
                        item.getDisplayName(),
                        GuiScrollableItemSelector.this.xPosition + 20,
                        slotTop + 5,
                        0xFFFFFF);
            } else if (GuiScrollableItemSelector.this.searchResults.get(slotIdx) instanceof ModSortingRule) {
                ModSortingRule modRule = (ModSortingRule) GuiScrollableItemSelector.this.searchResults.get(slotIdx);
                modRule.draw(GuiScrollableItemSelector.this.xPosition, slotTop, GuiScrollableItemSelector.this.zLevel);
            } else if (GuiScrollableItemSelector.this.searchResults.get(slotIdx) instanceof TrackableModCategoryPair) {
                TrackableModCategoryPair modWithItemCategory = (TrackableModCategoryPair)
                        GuiScrollableItemSelector.this.searchResults.get(slotIdx);
                GuiHelpers.drawMod(GuiScrollableItemSelector.this.xPosition,
                        slotTop, GuiScrollableItemSelector.this.zLevel, modWithItemCategory.getMod(), 20, 20);
                GuiScrollableItemSelector.this.privFontRenderer.drawString(
                        "(mod) " +  modWithItemCategory.getMod().getName() + ":" +
                                I18n.format(modWithItemCategory.getCategory().getTranslatedTabLabel()),
                        GuiScrollableItemSelector.this.xPosition + 20,
                        slotTop + 5,
                        0xFFFFFF);
            } else if (GuiScrollableItemSelector.this.searchResults.get(slotIdx) instanceof CreativeTabs) {
                CreativeTabs tab = (CreativeTabs) GuiScrollableItemSelector.this.searchResults.get(slotIdx);
                GuiHelpers.drawItem(GuiScrollableItemSelector.this.xPosition,
                        slotTop, tab.getIconItemStack(), GuiScrollableItemSelector.this.privFontRenderer);
                GuiScrollableItemSelector.this.privFontRenderer.drawString(
                        "(category) " + I18n.format(tab.getTranslatedTabLabel()),
                        GuiScrollableItemSelector.this.xPosition + 20,
                        slotTop + 5,
                        0xFFFFFF);
            } else {
                LOGGER.warn("Tried to slot a "+GuiScrollableItemSelector.this.searchResults.get(slotIdx).toString());
            }
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
