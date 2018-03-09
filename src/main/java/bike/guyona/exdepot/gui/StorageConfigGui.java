package bike.guyona.exdepot.gui;

import bike.guyona.exdepot.helpers.TrackableItemStack;
import bike.guyona.exdepot.gui.buttons.*;
import bike.guyona.exdepot.helpers.GuiHelpers;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.helpers.TrackableModCategoryPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.vecmath.Point2i;
import java.io.IOException;
import java.util.*;

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
    private List<ModContainer> modsValue;
    private LinkedHashSet<CreativeTabs> categoriesValue;
    private LinkedHashSet<TrackableModCategoryPair> modsCategoriesValue;
    private List<ItemStack> itemsValue;

    private static final String[] HEADERS = {
            "Mods:",
            "Categories:",
            "Mod+Categories:",
            "Items:"
    };
    private static final int RULE_OFFSET = 20;
    private static final int ICON_WIDTH = 20;

    enum EntryTypes
    {
        HEADER, MOD, ITEM_CATEGORY, MOD_WITH_ITEM_CATEGORY, ITEM
    }

    public StorageConfigGui() {
        buttonId = 0;
        allItemsValue = false;
        modsValue = new ArrayList<>();
        categoriesValue = new LinkedHashSet<>();
        modsCategoriesValue = new LinkedHashSet<>();
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
            for (ItemStack itemStack : itemsValue) {
                if (itemStack.getItem().getRegistryName().equals(newItem.getItem().getRegistryName())
                        && itemStack.getItemDamage() == newItem.getItemDamage())
                    return;
            }
            itemsValue.add(newItem);
        } else if (anyItem instanceof TrackableModCategoryPair) {
            modsCategoriesValue.add((TrackableModCategoryPair) anyItem);
        } else if (anyItem instanceof CreativeTabs) {
            categoriesValue.add((CreativeTabs)anyItem);
        }
    }

    public StorageConfig getStorageConfig() {
        StorageConfig config = new StorageConfig();
        config.allItems = allItemsValue;
        for (ItemStack item : itemsValue) {
            config.itemIds.add(new TrackableItemStack(item));
        }
        for (ModContainer mod : modsValue) {
            config.modIds.add(mod.getModId());
        }
        config.modIdAndCategoryPairs.addAll(modsCategoriesValue);
        for (CreativeTabs tab : categoriesValue) {
            config.itemCategories.add(tab.getTabLabel());
        }
        return config;
    }

    public void setStorageConfig(StorageConfig storageConfig) {
        modsValue.clear();
        itemsValue.clear();
        modsCategoriesValue.clear();
        categoriesValue.clear();

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
        for (TrackableItemStack itemId : storageConfig.itemIds) {
            Item item = Item.getByNameOrId(itemId.itemId);
            if (item == null) {
                LOGGER.error("No item with id: {}", itemId);
                continue;
            }
            ItemStack stack = item.getDefaultInstance();
            stack.setItemDamage(itemId.itemDamage);
            itemsValue.add(stack);
        }
        modsCategoriesValue.addAll(storageConfig.modIdAndCategoryPairs);
        for (CreativeTabs tab:CreativeTabs.CREATIVE_TAB_ARRAY) {
            if (storageConfig.itemCategories.contains(tab.getTabLabel())) {
                categoriesValue.add(tab);
            }
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
            searchField.clearSearchResults();
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        searchField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_ESCAPE) {
            Minecraft.getMinecraft().player.closeScreen();
        }
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

        private Point2i getSlotTypeAndIndex(int slotIdx) {
            if (slotIdx < 0)
                return new Point2i(0, -1);
            Collection<?>[] allRuleCollections = {
                    StorageConfigGui.this.modsValue,
                    StorageConfigGui.this.categoriesValue,
                    StorageConfigGui.this.modsCategoriesValue,
                    StorageConfigGui.this.itemsValue
            };
            EntryTypes type = EntryTypes.HEADER;
            int idx = -1;

            int lastListSize = 0;
            int curIdx = 0;
            for (Collection rules:allRuleCollections) {
                slotIdx -= lastListSize;
                lastListSize = rules.size() == 0 ? 0 : rules.size() + 1;
                if (lastListSize > 0 && slotIdx < lastListSize) {
                    if (slotIdx == 0) {
                        type = EntryTypes.HEADER;
                        idx = curIdx;
                    } else {
                        type = EntryTypes.values()[curIdx+1];
                        idx = slotIdx - 1;
                    }
                    break;
                }
                curIdx++;
            }
            return new Point2i(type.ordinal(), idx);
        }

        @Override
        public void handleMouseInput(int mouseX, int mouseY) throws IOException {
            super.handleMouseInput(mouseX, mouseY);
        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            Minecraft mc = Minecraft.getMinecraft();
            Point2i translatedIdx = getSlotTypeAndIndex(slotIdx);
            EntryTypes slotType = EntryTypes.values()[translatedIdx.x];
            int newIdx = translatedIdx.y;

            if (newIdx < 0) {
                LOGGER.warn("Tried to slot nothing at idx {}", slotIdx);
                return;
            }

            switch (slotType) {
                case HEADER:
                    String header = StorageConfigGui.HEADERS[newIdx];
                    mc.fontRendererObj.drawString(
                            header,
                            left,
                            slotTop + 5,
                            0xFFFFFF);
                    break;
                case MOD:
                    ModContainer mod = StorageConfigGui.this.modsValue.get(newIdx);
                    GuiHelpers.drawMod(left + StorageConfigGui.RULE_OFFSET,
                            slotTop, StorageConfigGui.this.zLevel, mod, 20, 20);
                    mc.fontRendererObj.drawString(
                            mod.getName(),
                            left + StorageConfigGui.ICON_WIDTH + StorageConfigGui.RULE_OFFSET,
                            slotTop + 5,
                            0xFFFFFF);
                    break;
                case ITEM_CATEGORY:
                    CreativeTabs category = (CreativeTabs) StorageConfigGui.this.categoriesValue.toArray()[newIdx];
                    GuiHelpers.drawItem(left + StorageConfigGui.RULE_OFFSET,
                            slotTop, category.getIconItemStack(), mc.fontRendererObj);
                    mc.fontRendererObj.drawString(
                            I18n.format(category.getTranslatedTabLabel()),
                            left + StorageConfigGui.ICON_WIDTH + StorageConfigGui.RULE_OFFSET,
                            slotTop + 5,
                            0xFFFFFF);
                    break;
                case MOD_WITH_ITEM_CATEGORY:
                    TrackableModCategoryPair modWithItemCategory = (TrackableModCategoryPair)
                            StorageConfigGui.this.modsCategoriesValue.toArray()[newIdx];
                    GuiHelpers.drawMod(left + StorageConfigGui.RULE_OFFSET, slotTop,
                            StorageConfigGui.this.zLevel, modWithItemCategory.getMod(), 20, 20);
                    mc.fontRendererObj.drawString(
                            modWithItemCategory.getMod().getName()+" : "+I18n.format(modWithItemCategory.getCategory().getTranslatedTabLabel()),
                            left + StorageConfigGui.ICON_WIDTH + StorageConfigGui.RULE_OFFSET,
                            slotTop + 5,
                            0xFFFFFF);
                    break;
                case ITEM:
                    ItemStack item = StorageConfigGui.this.itemsValue.get(newIdx);
                    GuiHelpers.drawItem(left + StorageConfigGui.RULE_OFFSET,
                            slotTop, item, mc.fontRendererObj);
                    mc.fontRendererObj.drawString(
                            item.getDisplayName(),
                            left + StorageConfigGui.ICON_WIDTH + StorageConfigGui.RULE_OFFSET,
                            slotTop + 5,
                            0xFFFFFF);
                    break;
            }
        }

        @Override
        protected void elementClicked(int slotIdx, boolean doubleClick) {
            Minecraft mc = Minecraft.getMinecraft();
            int mouseX = Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            if (StorageConfigGui.this.searchField.containsClick(mouseX, mouseY))
                return;

            Point2i translatedIdx = getSlotTypeAndIndex(slotIdx);
            EntryTypes slotType = EntryTypes.values()[translatedIdx.x];
            int newIdx = translatedIdx.y;

            if (newIdx < 0) {
                LOGGER.warn("Tried to click nothing at idx {}", slotIdx);
                return;
            }

            switch (slotType) {
                case HEADER:
                    break;
                case MOD:
                    StorageConfigGui.this.modsValue.remove(newIdx);
                    break;
                case ITEM_CATEGORY:
                    CreativeTabs category = (CreativeTabs) StorageConfigGui.this.categoriesValue.toArray()[newIdx];
                    StorageConfigGui.this.categoriesValue.remove(category);
                    break;
                case MOD_WITH_ITEM_CATEGORY:
                    TrackableModCategoryPair modWithItemCategory = (TrackableModCategoryPair)
                            StorageConfigGui.this.modsCategoriesValue.toArray()[newIdx];
                    StorageConfigGui.this.modsCategoriesValue.remove(modWithItemCategory);
                    break;
                case ITEM:
                    StorageConfigGui.this.itemsValue.remove(newIdx);
                    break;
            }
        }

        @Override protected int getSize() {
            return StorageConfigGui.this.modsValue.size() + StorageConfigGui.this.itemsValue.size() +
                    StorageConfigGui.this.modsCategoriesValue.size() + StorageConfigGui.this.categoriesValue.size() +
                    (StorageConfigGui.this.modsValue.size() == 0 ? 0 : 1) +
                    (StorageConfigGui.this.itemsValue.size() == 0 ? 0 : 1) +
                    (StorageConfigGui.this.modsCategoriesValue.size() == 0 ? 0 : 1) +
                    (StorageConfigGui.this.categoriesValue.size() == 0 ? 0 : 1);
        }

        @Override protected boolean isSelected(int index) {
            return false;
        }

        @Override protected void drawBackground() {}
    }
}
