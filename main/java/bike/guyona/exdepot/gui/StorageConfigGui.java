package bike.guyona.exdepot.gui;

import bike.guyona.exdepot.gui.buttons.*;
import bike.guyona.exdepot.helpers.GuiHelpers;
import bike.guyona.exdepot.capability.StorageConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

/**
 * Created by longb on 12/6/2017.
 */
public class StorageConfigGui extends GuiScreen {
    private static int MIN_ELEMENT_SEPARATION = 10;
    public static int BUTTON_HEIGHT = 20;
    private int buttonId;
    private GuiScrollableItemSelector searchField;
    private AllItemsButton allItemsToggle;
    private GuiButton ezConfigButton;
    private GuiButton saveConfigButton;
    private GuiButton clearConfigButton;
    private RulesList rulesBox;

    private boolean allItemsValue;
    private java.util.List<ModContainer> modsValue;
    private java.util.List<ItemStack> itemsValue;

    private static String MOD_RULES_HEADER = "Mods:";
    private static String ITEM_RULES_HEADER = "Items:";
    private static int RULE_OFFSET = 20;
    private static int ICON_WIDTH = 20;

    public StorageConfigGui() {
        buttonId = 0;
        allItemsValue = false;
        modsValue = new ArrayList<>();
        itemsValue = new ArrayList<>();
    }

    public void initGui() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int xOffset = 0;
        int prevItemWidth = 0;
        searchField = new GuiScrollableItemSelector(buttonId++, fr,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=200, BUTTON_HEIGHT, 150, this.width, this.height, this);
        // Create my buttons
        int firstButtonOffset = xOffset + prevItemWidth;
        allItemsToggle = new AllItemsButton(buttonId++,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=20, BUTTON_HEIGHT);
        ezConfigButton = new FromInventoryButton(buttonId++,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=20, BUTTON_HEIGHT);
        saveConfigButton = new SaveButton(buttonId++,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=20, BUTTON_HEIGHT);
        clearConfigButton = new ClearButton(buttonId++,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=20, BUTTON_HEIGHT);
        prevItemWidth = 0;
        rulesBox = new RulesList(width - 2 * MIN_ELEMENT_SEPARATION,
                height - MIN_ELEMENT_SEPARATION * 3 - BUTTON_HEIGHT,
                MIN_ELEMENT_SEPARATION*2+BUTTON_HEIGHT,
                this.height-MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION,
                BUTTON_HEIGHT);
        // Add my buttons, in reverse so tooltips render on top of buttons further to the right.
        buttonList.add(clearConfigButton);
        buttonList.add(saveConfigButton);
        buttonList.add(ezConfigButton);
        buttonList.add(allItemsToggle);
    }

    public void addConfigItem(Object anyItem) {
        if (anyItem instanceof ModContainer) {
            ModContainer newMod = (ModContainer)anyItem;
            for (ModContainer mod : modsValue) {
                if (mod.getModId().equals(newMod.getModId()))
                    return;
            }
            modsValue.add(newMod);
        } else if (anyItem instanceof ItemStack) {
            ItemStack newItem = (ItemStack)anyItem;
            for (ItemStack item : itemsValue) {
                if (item.getUnlocalizedName().equals(newItem.getUnlocalizedName()))
                    return;
            }
            itemsValue.add(newItem);
        }
    }

    public StorageConfig getStorageConfig() {
        StorageConfig config = new StorageConfig();
        config.allItems = allItemsValue;
        for (ItemStack item : itemsValue) {
            config.itemIds.add(Item.REGISTRY.getIDForObject(item.getItem()));
        }
        for (ModContainer mod : modsValue) {
            config.modIds.add(mod.getModId());
        }
        return config;
    }

    public void setStorageConfig(StorageConfig storageConfig) {
        modsValue.clear();
        itemsValue.clear();
        allItemsValue = storageConfig.allItems;
        allItemsToggle.setToggle(allItemsValue);
        Loader loader = Loader.instance();
        for (String modId : storageConfig.modIds) {
            for(ModContainer mod : loader.getModList()) {
                if (modId.equals(mod.getModId())) {
                    modsValue.add(mod);
                    break;
                }
            }
        }
        for (int itemId : storageConfig.itemIds) {
            Item item = Item.REGISTRY.getObjectById(itemId);
            if (item == null) {
                LOGGER.error("No item with id: {}", itemId);
                continue;
            }
            ItemStack stack = new ItemStack(item, 1);
            itemsValue.add(stack);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        rulesBox.drawScreen(mouseX, mouseY, partialTicks);
        searchField.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        Minecraft mc = Minecraft.getMinecraft();
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        if (searchField.containsClick(mouseX, mouseY)) {
            searchField.handleMouseInput();
        }else {
            rulesBox.handleMouseInput(mouseX, mouseY);
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (searchField.containsClick(mouseX, mouseY)) {
            searchField.mouseClicked(mouseX, mouseY, mouseButton);
        } else {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
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

        private Object getItem(int slotIdx) {
            java.util.List<ModContainer> mods = StorageConfigGui.this.modsValue;
            java.util.List<ItemStack> items = StorageConfigGui.this.itemsValue;
            int modsEnd = mods.size() == 0 ? 0 : mods.size() + 1;
            Object anyItem = null;
            if (slotIdx == 0 && mods.size() != 0) {
                anyItem = StorageConfigGui.MOD_RULES_HEADER;
            } else if (slotIdx <= mods.size() && mods.size() != 0) {
                anyItem = mods.get(slotIdx - 1);
            } else if (slotIdx == modsEnd && items.size() != 0) {
                anyItem = StorageConfigGui.ITEM_RULES_HEADER;
            } else if (slotIdx <= modsEnd + items.size() && items.size() != 0) {
                anyItem = items.get(slotIdx - 1 - modsEnd);
            }
            return anyItem;
        }

        @Override
        public void handleMouseInput(int mouseX, int mouseY) throws IOException {
            super.handleMouseInput(mouseX, mouseY);
        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            Minecraft mc = Minecraft.getMinecraft();
            Object anyItem = getItem(slotIdx);

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
            Minecraft mc = Minecraft.getMinecraft();
            int mouseX = Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            if (StorageConfigGui.this.searchField.containsClick(mouseX, mouseY))
                return;
            Object anyItem = getItem(index);
            if (anyItem instanceof ItemStack) {
                ItemStack item = (ItemStack)anyItem;
                StorageConfigGui.this.itemsValue.remove(item);
            } else if (anyItem instanceof ModContainer) {
                ModContainer mod = (ModContainer)anyItem;
                StorageConfigGui.this.modsValue.remove(mod);
            } else if (anyItem instanceof String) {
                // Do nothing.
            } else {
                LOGGER.warn("Tried to remove a " + (anyItem == null ? "NULL" : anyItem.toString()));
            }
        }

        @Override protected int getSize() {
            return StorageConfigGui.this.modsValue.size() + StorageConfigGui.this.itemsValue.size() +
                    (StorageConfigGui.this.modsValue.size() == 0 ? 0 : 1) +
                    (StorageConfigGui.this.itemsValue.size() == 0 ? 0 : 1);
        }

        @Override protected boolean isSelected(int index) {
            return false;
        }

        @Override protected void drawBackground() {}
    }
}
