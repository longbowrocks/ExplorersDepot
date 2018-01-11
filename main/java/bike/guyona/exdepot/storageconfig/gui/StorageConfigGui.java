package bike.guyona.exdepot.storageconfig.gui;

import bike.guyona.exdepot.helpers.GuiHelpers;
import bike.guyona.exdepot.storageconfig.capability.StorageConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.common.ModContainer;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

/**
 * Created by longb on 12/6/2017.
 */
public class StorageConfigGui extends GuiScreen {
    private static int MIN_ELEMENT_SEPARATION = 10;
    public static int BUTTON_HEIGHT = 20;
    private int buttonId;
    private GuiScrollableItemSelector searchField;
    private GuiButton allItemsToggle;
    private GuiButton ezConfigButton;
    private GuiButton saveConfigButton;
    private GuiButton clearConfigButton;

    private StorageConfig storageConfig;

    private static String MOD_RULES_HEADER = "Mods:";
    private static String ITEM_RULES_HEADER = "Items:";
    private static int RULE_OFFSET = 20;
    private static int ICON_WIDTH = 20;

    public StorageConfigGui() {
        buttonId = 0;
    }

    public void initGui() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int xOffset = 0;
        int prevItemWidth = 0;
        searchField = new GuiScrollableItemSelector(buttonId++, fr,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=200, BUTTON_HEIGHT, 150, this.width, this.height);
        // Create my buttons
        int firstButtonOffset = xOffset + prevItemWidth;
        allItemsToggle = new GuiButton(buttonId++,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=50, BUTTON_HEIGHT, "All Items");
        ezConfigButton = new GuiButton(buttonId++,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=100, BUTTON_HEIGHT, "Set From Contents");
        prevItemWidth = 0;
        xOffset = firstButtonOffset;
        saveConfigButton = new GuiButton(buttonId++,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION*2+BUTTON_HEIGHT, prevItemWidth=50, BUTTON_HEIGHT, "Save");
        clearConfigButton = new GuiButton(buttonId++,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION*2+BUTTON_HEIGHT, prevItemWidth=50, BUTTON_HEIGHT, "Clear");
        // Add my buttons.
        buttonList.add(allItemsToggle);
        buttonList.add(ezConfigButton);
        buttonList.add(saveConfigButton);
        buttonList.add(clearConfigButton);
    }

    public void setStorageConfig(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawRect(MIN_ELEMENT_SEPARATION,MIN_ELEMENT_SEPARATION*3+BUTTON_HEIGHT*2,
                this.width-MIN_ELEMENT_SEPARATION,this.height-MIN_ELEMENT_SEPARATION,
                0xFF000000);
        searchField.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        searchField.handleMouseInput();
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        searchField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private class RulesList extends GuiScrollingList {
        public RulesList(int width, int height, int top, int bottom, int left, int entryHeight)
        {
            super(Minecraft.getMinecraft(), width, height, top, bottom, left, entryHeight,
                    Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);

            this.setHeaderInfo(false, 0);
        }

        @Override
        public void handleMouseInput(int mouseX, int mouseY) throws IOException {
            super.handleMouseInput(mouseX, mouseY);
        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            Minecraft mc = Minecraft.getMinecraft();
            StorageConfig storageConfig = StorageConfigGui.this.storageConfig;
            if (storageConfig == null) {
                LOGGER.error("There's no storageConfig in StorageConfigGui");
                return;
            }
            int modsEnd = storageConfig.modIds.size() == 0 ? 0 : storageConfig.modIds.size() + 1;
            Object anyItem = null;
            if (slotIdx == 0 && storageConfig.modIds.size() != 0) {
                anyItem = StorageConfigGui.MOD_RULES_HEADER;
            } else if (slotIdx <= storageConfig.modIds.size()) {
                anyItem = storageConfig.modIds.get(slotIdx-1);
            } else if (slotIdx == modsEnd && storageConfig.itemIds.size() != 0) {
                anyItem = StorageConfigGui.ITEM_RULES_HEADER;
            } else if (slotIdx <= modsEnd + storageConfig.itemIds.size()) {
                anyItem = storageConfig.itemIds.get(slotIdx - modsEnd);
            }

            if (anyItem instanceof ItemStack) {
                ItemStack item = (ItemStack)anyItem;
                GuiHelpers.drawItem(left + StorageConfigGui.RULE_OFFSET,
                        slotTop, item, mc.fontRendererObj);
                mc.fontRendererObj.drawString(
                        item.getDisplayName(),
                        left + StorageConfigGui.ICON_WIDTH + StorageConfigGui.RULE_OFFSET,
                        slotTop + 5,
                        0xFFFFFF);
            } else if (anyItem instanceof ModContainer) {
                ModContainer mod = (ModContainer)anyItem;
                GuiHelpers.drawMod(left + StorageConfigGui.RULE_OFFSET,
                        slotTop, StorageConfigGui.this.zLevel, mod, 20, 20);
                mc.fontRendererObj.drawString(
                        mod.getName(),
                        left + StorageConfigGui.ICON_WIDTH + StorageConfigGui.RULE_OFFSET,
                        slotTop + 5,
                        0xFFFFFF);
            } else if (anyItem instanceof String) {
                String header = (String)anyItem;
                mc.fontRendererObj.drawString(
                        header,
                        left,
                        slotTop + 5,
                        0xFFFFFF);
            } else {
                LOGGER.warn("Tried to slot a " + (anyItem == null ? "NULL" : anyItem.toString()));
            }
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {

        }

        @Override protected int getSize() {
            StorageConfig storageConfig = StorageConfigGui.this.storageConfig;
            return storageConfig.modIds.size() + storageConfig.itemIds.size() +
                    (storageConfig.modIds.size() == 0 ? 0 : 1) + (storageConfig.itemIds.size() == 0 ? 0 : 1);
        }

        @Override protected boolean isSelected(int index) {
            return false;
        }

        @Override protected void drawBackground() {}
    }
}
