package bike.guyona.exdepot.gui;

import bike.guyona.exdepot.helpers.GuiHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;


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
        Loader loader = Loader.instance();
        for(ModContainer mod : loader.getModList()) {
            if (mod.getModId().startsWith(getText()) || mod.getName().startsWith(getText())) {
                searchResults.add(mod);
            }
        }
        for(Item item : Item.REGISTRY) {
            ItemStack stack = new ItemStack(item, 1);
            if (stack.getDisplayName().toLowerCase().contains(getText())) {
                searchResults.add(stack);
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
            } else if (GuiScrollableItemSelector.this.searchResults.get(slotIdx) instanceof ModContainer) {
                ModContainer mod = (ModContainer) GuiScrollableItemSelector.this.searchResults.get(slotIdx);
                GuiHelpers.drawMod(GuiScrollableItemSelector.this.xPosition,
                        slotTop, GuiScrollableItemSelector.this.zLevel, mod, 20, 20);
                GuiScrollableItemSelector.this.privFontRenderer.drawString(
                        "(mod) " + mod.getName(),
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
