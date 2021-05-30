package bike.guyona.exdepot.gui;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.gui.buttons.*;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.interfaces.IHasTooltip;
import bike.guyona.exdepot.gui.notbuttons.GuiScrollableItemSelector;
import bike.guyona.exdepot.helpers.AccessHelpers;
import bike.guyona.exdepot.helpers.GuiHelpers;
import bike.guyona.exdepot.sortingrules.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

/**
 * Created by longb on 12/6/2017.
 */
public class StorageConfigGui extends ChatScreen {  // TODO: probably not the best superclass
    private static int MIN_ELEMENT_SEPARATION = 10;
    public static int BUTTON_HEIGHT = 20;
    private GuiScrollableItemSelector searchField;
    private AdvancedTooltipsButton advancedTooltipsToggle;
    private UseNbtButton useNbtToggle;
    private AllItemsButton allItemsToggle;
    private SmartFromInventoryButton smartEzConfigButton;
    private GuiIconButton ezConfigButton;
    private GuiIconButton saveConfigButton;
    private GuiIconButton clearConfigButton;
    private RulesList rulesBox;

    private boolean advancedTooltipsValue;
    private List<IHasTooltip> tooltippedObjects;
    private StorageConfig configValue;
    private static final int RULE_OFFSET = 20;
    public static final int ICON_WIDTH = 20;

    public StorageConfigGui(String titleIn) {
        super(titleIn);
        advancedTooltipsValue = false;
        tooltippedObjects = new ArrayList<>();
        configValue = new StorageConfig();
    }

    public void initGui() {
        FontRenderer fr = getMinecraft().fontRenderer;
        int xOffset = 0;
        int prevItemWidth = 0;
        searchField = new GuiScrollableItemSelector(fr,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=200, BUTTON_HEIGHT, 150, this.width, this.height, this);
        // Create my buttons
        int firstButtonOffset = xOffset + prevItemWidth;
        allItemsToggle = new AllItemsButton(
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=20, BUTTON_HEIGHT);
        ezConfigButton = new FromInventoryButton(
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=20, BUTTON_HEIGHT);
        smartEzConfigButton = new SmartFromInventoryButton(
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=20, BUTTON_HEIGHT);
        saveConfigButton = new SaveButton(
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=20, BUTTON_HEIGHT);
        clearConfigButton = new ClearButton(
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=20, BUTTON_HEIGHT);
        useNbtToggle = new UseNbtButton(
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=20, BUTTON_HEIGHT);
        advancedTooltipsToggle = new AdvancedTooltipsButton(
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=20, BUTTON_HEIGHT);
        prevItemWidth = 0;
        rulesBox = new RulesList(width - 2 * MIN_ELEMENT_SEPARATION,
                height - MIN_ELEMENT_SEPARATION * 3 - BUTTON_HEIGHT,
                MIN_ELEMENT_SEPARATION*2+BUTTON_HEIGHT,
                this.height-MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION,
                BUTTON_HEIGHT);

        List<Button> buttonList = AccessHelpers.getButtonList(this);
        if (buttonList == null) {
            LOGGER.error("Oh gods what has gone wrong with the button list.");
        }
        buttonList.add(clearConfigButton);
        buttonList.add(saveConfigButton);
        buttonList.add(ezConfigButton);
        buttonList.add(allItemsToggle);
        buttonList.add(useNbtToggle);
        buttonList.add(advancedTooltipsToggle);
        buttonList.add(smartEzConfigButton);

        tooltippedObjects.add(searchField);
        tooltippedObjects.add(rulesBox);
        tooltippedObjects.add(clearConfigButton);
        tooltippedObjects.add(saveConfigButton);
        tooltippedObjects.add(ezConfigButton);
        tooltippedObjects.add(allItemsToggle);
        tooltippedObjects.add(useNbtToggle);
        tooltippedObjects.add(advancedTooltipsToggle);
        tooltippedObjects.add(advancedTooltipsToggle);
        tooltippedObjects.add(smartEzConfigButton);
    }

    public void addConfigItem(AbstractSortingRule anyItem) {
        configValue.addRule(anyItem);
    }

    public boolean getShowAdvancedTooltips() {
        return advancedTooltipsValue;
    }

    public void setShowAdvancedTooltips(boolean newValue) {
        advancedTooltipsValue = newValue;
    }

    public StorageConfig getStorageConfig() {
        return configValue;
    }

    public void setStorageConfig(StorageConfig storageConfig) {
        configValue = storageConfig;
        useNbtToggle.setToggle(configValue.getUseNbt());
        allItemsToggle.setToggle(configValue.allItems);
    }

    public void setChestPosition(BlockPos chestPos) {
        ((SaveButton)saveConfigButton).setChestPosition(chestPos);
        ((FromInventoryButton)ezConfigButton).setChestPosition(chestPos);
        smartEzConfigButton.setChestPosition(chestPos);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        rulesBox.render(mouseX, mouseY, partialTicks);
        searchField.drawScreen(mouseX, mouseY, partialTicks);
        // text has no Z, so it needs to render after everything else to go on top.
        for (IHasTooltip tooltippedObj : tooltippedObjects) {
            if (tooltippedObj.containsClick(mouseX, mouseY)) {
                GuiHelpers.drawTooltip(tooltippedObj, mouseX, mouseY, advancedTooltipsValue);
                break;
            }
        }
    }

//    @Override
//    public void handleMouseInput() throws IOException {
//        super.handleMouseInput();
//        Minecraft mc = Minecraft.getInstance();
//        Point mouse = MouseInfo.getPointerInfo().getLocation();
//        int mouseX = mouse.x * width / mc.currentScreen.width;
//        int mouseY = height - mouse.y * height / mc.currentScreen.height - 1;
//        if (searchField.containsClick(mouseX, mouseY)) {
//            searchField.handleMouseInput();
//        }else {
//            rulesBox.handleMouseInput(mouseX, mouseY);
//        }
//    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (searchField.containsClick(mouseX, mouseY)) {
            searchField.mouseClicked(mouseX, mouseY, mouseButton);
        } else {
            searchField.clearSearchResults();
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        boolean success = searchField.charTyped(typedChar, keyCode);
        // TODO: safe? https://github.com/mezz/JustEnoughItems/blob/9d68f03939f7723b3c3abd30ccc719025adc9010/src/main/java/mezz/jei/config/KeyBindings.java#L70
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().player.closeScreen();
        }
        return success;
    }

    private class RulesList extends OptionsRowList implements IHasTooltip {
        private String longTooltip;
        private String longTooltipCache;

        public RulesList(int width, int height, int top, int bottom, int left, int entryHeight)
        {
            super(Minecraft.getInstance(), width, height, top, bottom, entryHeight);

            longTooltip = "exdepot.tooltip.rulespanel.adv";
            longTooltipCache = null;
        }

        private Point getSlotTypeAndIndex(int slotIdx) {
            if (slotIdx < 0)
                return new Point(0, -1);
            int ruleTypeIdx = -1;
            int ruleIdx = -1;

            int lastListSize = 0;
            // For now, I don't need a custom list to define rule display order. Lower priority rules should
            // display first, so show in reverse priority order.
            for (int i = ExDepotMod.sortingRuleProvider.ruleClasses.size()-1; i>=0; i--) {
                slotIdx -= lastListSize;
                Set<? extends AbstractSortingRule> rules = configValue.getRules(ExDepotMod.sortingRuleProvider.ruleClasses.get(i));
                lastListSize = (rules == null || rules.size() == 0) ? 0 : rules.size() + 1;
                if (lastListSize > 0 && slotIdx < lastListSize) {
                    ruleTypeIdx = i;
                    ruleIdx = slotIdx - 1;
                    break;
                }
            }
            return new Point(ruleTypeIdx, ruleIdx);
        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            Minecraft mc = Minecraft.getInstance();
            Point translatedIdx = getSlotTypeAndIndex(slotIdx);
            int ruleTypeIdx = translatedIdx.x;
            int ruleIdx = translatedIdx.y;

            if (ruleTypeIdx < 0) {
                LOGGER.warn("Tried to draw nothing at idx {}", slotIdx);
                return;
            }
            Class<? extends AbstractSortingRule> ruleClass = ExDepotMod.sortingRuleProvider.ruleClasses.get(ruleTypeIdx);
            if (ruleIdx < 0) {
                String header = ExDepotMod.sortingRuleProvider.getRuleTypeDisplayName(ruleClass);
                mc.fontRenderer.drawString(
                        header,
                        getLeft(),
                        slotTop + 5,
                        0xFFFFFF);
            } else {
                AbstractSortingRule rule = (AbstractSortingRule) configValue.getRules(ruleClass).toArray()[ruleIdx];
                rule.draw(x0 + StorageConfigGui.RULE_OFFSET, slotTop, Ref.Z_LEVEL);
            }
        }

        @Override
        protected void elementClicked(int slotIdx, boolean doubleClick) {
            Minecraft mc = Minecraft.getInstance();
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            int mouseX = mouse.x * width / mc.currentScreen.width;
            int mouseY = height - mouse.y * height / mc.currentScreen.height - 1;
            if (StorageConfigGui.this.searchField.containsClick(mouseX, mouseY)) {
                return;
            }

            Point translatedIdx = getSlotTypeAndIndex(slotIdx);
            int ruleTypeIdx = translatedIdx.x;
            int ruleIdx = translatedIdx.y;

            if (ruleTypeIdx < 0) {
                LOGGER.warn("Tried to click nothing at idx {}", slotIdx);
                return;
            }
            Class<? extends AbstractSortingRule> ruleClass = ExDepotMod.sortingRuleProvider.ruleClasses.get(ruleTypeIdx);
            if (ruleIdx >= 0) {
                AbstractSortingRule rule = (AbstractSortingRule) configValue.getRules(ruleClass).toArray()[ruleIdx];
                configValue.getRules(ruleClass).remove(rule);
            }
        }

        @Override protected int getSize() {
            int totalSize = 0;
            for (int i=0; i<ExDepotMod.sortingRuleProvider.ruleClasses.size(); i++) {
                Set<? extends AbstractSortingRule> rulesList = configValue.getRules(ExDepotMod.sortingRuleProvider.ruleClasses.get(i));
                if (rulesList != null && rulesList.size() > 0) {
                    totalSize += rulesList.size() + 1;
                }
            }
            return totalSize;
        }

        @Override
        public String getTooltip() {
            return null;
        }

        @Override
        public String getLongTooltip() {
            if (longTooltipCache == null) {
                longTooltipCache = new TranslationTextComponent(longTooltip,
                        TextFormatting.GOLD,
                        TextFormatting.BLUE,
                        TextFormatting.LIGHT_PURPLE,
                        TextFormatting.YELLOW,
                        TextFormatting.RESET).getUnformattedComponentText();
            }
            return longTooltipCache;
        }

        @Override
        public boolean containsClick(int mouseX, int mouseY) {
            return mouseX > x0 && mouseX < x1 &&
                    mouseY > y0 && mouseY < y1;
        }
    }
}
