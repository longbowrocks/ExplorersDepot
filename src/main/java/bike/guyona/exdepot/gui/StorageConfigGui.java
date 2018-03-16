package bike.guyona.exdepot.gui;

import bike.guyona.exdepot.gui.buttons.*;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.sortingrules.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.vecmath.Point2i;
import java.io.IOException;
import java.util.*;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;

/**
 * Created by longb on 12/6/2017.
 */
public class StorageConfigGui extends GuiScreen {
    private static int MIN_ELEMENT_SEPARATION = 10;
    public static int BUTTON_HEIGHT = 20;
    private int buttonId;
    private GuiScrollableItemSelector searchField;
    private AdvancedTooltipsButton advancedTooltipsToggle;
    private UseNbtButton useNbtToggle;
    private AllItemsButton allItemsToggle;
    private GuiButton ezConfigButton;
    private GuiButton saveConfigButton;
    private GuiButton clearConfigButton;
    private RulesList rulesBox;

    private boolean advancedTooltipsValue;
    private StorageConfig configValue;
    private static final int RULE_OFFSET = 20;
    public static final int ICON_WIDTH = 20;

    public StorageConfigGui() {
        buttonId = 0;
        advancedTooltipsValue = false;
        configValue = new StorageConfig();
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
        useNbtToggle = new UseNbtButton(buttonId++,
                xOffset+=prevItemWidth+MIN_ELEMENT_SEPARATION,
                MIN_ELEMENT_SEPARATION, prevItemWidth=20, BUTTON_HEIGHT);
        advancedTooltipsToggle = new AdvancedTooltipsButton(buttonId++,
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
        buttonList.add(useNbtToggle);
        buttonList.add(advancedTooltipsToggle);
    }

    public void addConfigItem(AbstractSortingRule anyItem) {
        // TODO: if it's an item rule, we need to set the nbt flag based on current nbt settings.
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
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        rulesBox.drawScreen(mouseX, mouseY, partialTicks);
        searchField.drawScreen(mouseX, mouseY, partialTicks);
        for (GuiButton btn : buttonList) {
            if (btn instanceof GuiIconButton) {
                GuiIconButton iconButton = (GuiIconButton) btn;
                if (iconButton.containsClick(mouseX, mouseY)) {
                    iconButton.drawTooltip(mouseX, mouseY, advancedTooltipsValue); // pretty sure text uses painter's alg, so it needs to render late to go on top
                }
            }
        }
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
            int ruleTypeIdx = -1;
            int ruleIdx = -1;

            int lastListSize = 0;
            // For now, I don't need a custom list to define rule display order. Lower priority rules should
            // display first, so show in reverse priority order.
            for (int i=StorageConfig.ruleClasses.size()-1; i>=0; i--) {
                slotIdx -= lastListSize;
                Set<? extends AbstractSortingRule> rules = configValue.getRules(StorageConfig.ruleClasses.get(i));
                lastListSize = (rules == null || rules.size() == 0) ? 0 : rules.size() + 1;
                if (lastListSize > 0 && slotIdx < lastListSize) {
                    ruleTypeIdx = i;
                    ruleIdx = slotIdx - 1;
                    break;
                }
            }
            return new Point2i(ruleTypeIdx, ruleIdx);
        }

        @Override
        public void handleMouseInput(int mouseX, int mouseY) throws IOException {
            super.handleMouseInput(mouseX, mouseY);
        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            Minecraft mc = Minecraft.getMinecraft();
            Point2i translatedIdx = getSlotTypeAndIndex(slotIdx);
            int ruleTypeIdx = translatedIdx.x;
            int ruleIdx = translatedIdx.y;

            if (ruleTypeIdx < 0) {
                LOGGER.warn("Tried to slot nothing at idx {}", slotIdx);
                return;
            }
            Class<? extends AbstractSortingRule> ruleClass = StorageConfig.ruleClasses.get(ruleTypeIdx);
            if (ruleIdx < 0) {
                String header = proxy.sortingRuleProvider.getRuleTypeDisplayName(ruleClass);
                mc.fontRendererObj.drawString(
                        header,
                        left,
                        slotTop + 5,
                        0xFFFFFF);
            } else {
                AbstractSortingRule rule = (AbstractSortingRule) configValue.getRules(ruleClass).toArray()[ruleIdx];
                rule.draw(left + StorageConfigGui.RULE_OFFSET, slotTop, StorageConfigGui.this.zLevel);
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
            int ruleTypeIdx = translatedIdx.x;
            int ruleIdx = translatedIdx.y;

            if (ruleTypeIdx < 0) {
                LOGGER.warn("Tried to slot nothing at idx {}", slotIdx);
                return;
            }
            Class<? extends AbstractSortingRule> ruleClass = StorageConfig.ruleClasses.get(ruleTypeIdx);
            if (ruleIdx >= 0) {
                AbstractSortingRule rule = (AbstractSortingRule) configValue.getRules(ruleClass).toArray()[ruleIdx];
                configValue.getRules(ruleClass).remove(rule);
            }
        }

        @Override protected int getSize() {
            int totalSize = 0;
            for (int i=0; i<StorageConfig.ruleClasses.size(); i++) {
                Set<? extends AbstractSortingRule> rulesList = configValue.getRules(StorageConfig.ruleClasses.get(i));
                if (rulesList != null && rulesList.size() > 0) {
                    totalSize += rulesList.size() + 1;
                }
            }
            return totalSize;
        }

        @Override protected boolean isSelected(int index) {
            return false;
        }

        @Override protected void drawBackground() {}
    }
}
