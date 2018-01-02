package bike.guyona.exdepot.storageconfig.gui;

import bike.guyona.exdepot.helpers.GuiHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;


/**
 * Created by longb on 12/7/2017.
 */
public class GuiScrollableClickableItemSelector extends GuiTextField {
    private int mainGuiWidth;
    private int mainGuiHeight;
    private List<Object> searchResults;
    private GuiScrollingList resultListGui;
    private int maxListHeight;
    private FontRenderer privFontRenderer; // I could change the asm fontRendererInstance to public, but no thanks.

    public GuiScrollableClickableItemSelector(int componentId, FontRenderer fr, int x, int y, int width, int height,
                                              int maxHeight, int mainGuiWidth, int mainGuiHeight) {
        super(componentId, fr, x, y, width, height);
        this.mainGuiWidth = mainGuiWidth;
        this.mainGuiHeight = mainGuiHeight;
        this.privFontRenderer = fr;
        this.maxListHeight = maxHeight;
        searchResults = new ArrayList<>();
        resultListGui = new ResultList(null, null);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawTextBox();
        resultListGui.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void handleMouseInput() throws IOException {
        Minecraft mc = Minecraft.getMinecraft();
        int mouseX = Mouse.getEventX() * this.mainGuiWidth / mc.displayWidth;
        int mouseY = this.mainGuiHeight - Mouse.getEventY() * this.mainGuiHeight / mc.displayHeight - 1;
        resultListGui.handleMouseInput(mouseX, mouseY);
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

    private void updateSearchResults() {
        searchResults.clear();
        Loader loader = Loader.instance();
        for(ModContainer mod : loader.getModList()) {
            if (mod.getModId().startsWith(getText()) || mod.getName().startsWith(getText())) {
                searchResults.add(mod);
            }
        }
        // Either of these will get the item registry
        //GameRegistry.findRegistry(Item.class);
        //Item.REGISTRY;
        for(Item item : Item.REGISTRY) {
            ItemStack stack = new ItemStack(item, 1);
            if (stack.getUnlocalizedName().startsWith("item." + getText()) || stack.getDisplayName().startsWith(getText())) {
                searchResults.add(stack);
            }
        }
    }

    private class ResultList extends GuiScrollingList {
        public ResultList(@Nullable ResourceLocation logoPath, Dimension logoDims)
        {
            super(Minecraft.getMinecraft(),
                    GuiScrollableClickableItemSelector.this.width,
                    GuiScrollableClickableItemSelector.this.height,
                    GuiScrollableClickableItemSelector.this.yPosition + GuiScrollableClickableItemSelector.this.height,
                    GuiScrollableClickableItemSelector.this.yPosition + GuiScrollableClickableItemSelector.this.height + GuiScrollableClickableItemSelector.this.maxListHeight,
                    GuiScrollableClickableItemSelector.this.xPosition,
                    20,
                    Minecraft.getMinecraft().displayWidth,
                    Minecraft.getMinecraft().displayHeight);

            this.setHeaderInfo(false, 0);
        }

        @Override
        public void handleMouseInput(int mouseX, int mouseY) throws IOException {
            super.handleMouseInput(mouseX, mouseY);
        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            Minecraft mc = Minecraft.getMinecraft();
            if (GuiScrollableClickableItemSelector.this.searchResults.get(slotIdx) instanceof ItemStack) {
                ItemStack item = (ItemStack)GuiScrollableClickableItemSelector.this.searchResults.get(slotIdx);
                GuiHelpers.drawItem(GuiScrollableClickableItemSelector.this.xPosition,
                        slotTop, item, GuiScrollableClickableItemSelector.this.privFontRenderer);
                GuiScrollableClickableItemSelector.this.privFontRenderer.drawString(
                        item.getDisplayName(),
                        GuiScrollableClickableItemSelector.this.xPosition + 20,
                        slotTop + 5,
                        0xFFFFFF);
            } else if (GuiScrollableClickableItemSelector.this.searchResults.get(slotIdx) instanceof ModContainer) {
                ModContainer mod = (ModContainer) GuiScrollableClickableItemSelector.this.searchResults.get(slotIdx);
                GuiHelpers.drawMod(GuiScrollableClickableItemSelector.this.xPosition,
                        slotTop, GuiScrollableClickableItemSelector.this.zLevel, mod, 20, 20);
                GuiScrollableClickableItemSelector.this.privFontRenderer.drawString(
                        "(mod) " + mod.getName(),
                        GuiScrollableClickableItemSelector.this.xPosition + 20,
                        slotTop + 5,
                        0xFFFFFF);
            } else {
                LOGGER.warn("Tried to slot a "+GuiScrollableClickableItemSelector.this.searchResults.get(slotIdx).toString());
            }
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {

        }

        @Override protected int getSize() {
            return GuiScrollableClickableItemSelector.this.searchResults.size();
        }
        @Override protected boolean isSelected(int index) {
            return false;
        }
        @Override protected void drawBackground() {}
    }
}
