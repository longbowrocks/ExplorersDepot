package bike.guyona.exdepot.client.gui;

import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.client.gui.buttons.ExDepotImageButton;
import bike.guyona.exdepot.client.gui.selectors.RulesList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.ParametersAreNonnullByDefault;

public class DepotRulesScreen extends Screen {
    public static final int COLOR_WHITE_OPACITY_NONE = constructAlphaRGB((byte)0, (byte)255, (byte)255, (byte)255);
    public static final int COLOR_BLACK_OPACITY_MEDIUM = constructAlphaRGB((byte)192, (byte)16,(byte)16,(byte)16);
    public static final int COLOR_BLACK_OPACITY_HEAVY = constructAlphaRGB((byte)208, (byte)16,(byte)16,(byte)16);
    public static final int COLOR_DARK_GREEN_OPACITY_HEAVY = constructAlphaRGB((byte)208, (byte)16,(byte)32,(byte)16);

    private static final int MIN_ELEMENT_SEPARATION = 10;

    private boolean hasUnsavedChanges;
    private IDepotCapability savedDepotRules;
    private IDepotCapability depotRules;

    private EditBox searchField;
    private ImageButton allItemsToggle;
    private ImageButton ezConfigButton;
    private ImageButton saveConfigButton;
    private ImageButton clearConfigButton;
    private RulesList rulesBox;

    public DepotRulesScreen(Component parentScreen) {
        super(new TranslatableComponent("exdepot.gui.depotrules.title"));
        this.hasUnsavedChanges = false;
    }

    /**
     * Initializes GUI state (ie widgets) from data state (ie DepotCapability).
     */
    @Override
    protected void init() {
        Font fr = Minecraft.getInstance().font;
        int xOffset = MIN_ELEMENT_SEPARATION;
        int yOffset = MIN_ELEMENT_SEPARATION;
        searchField = new EditBox(fr,
                xOffset, yOffset, 200, ExDepotImageButton.BUTTON_HEIGHT, new TextComponent("Hi there!"));
        xOffset += MIN_ELEMENT_SEPARATION + searchField.getWidth();
        this.setFocused(searchField);
        searchField.setFocus(true);
        // Create my buttons
        clearConfigButton = new ExDepotImageButton(
                xOffset, yOffset, ExDepotImageButton.FLOPPY_DISK_BIDX,
                (button) -> {},
                new TranslatableComponent("exdepot.gui.depotrules.tooltip.clear"),
                this
        );
        xOffset += MIN_ELEMENT_SEPARATION + clearConfigButton.getWidth();
        saveConfigButton = new ExDepotImageButton(
                xOffset, yOffset, ExDepotImageButton.FLOPPY_DISK_BIDX,
                (button) -> {},
                new TranslatableComponent("exdepot.gui.depotrules.tooltip.save"),
                this
        );
        xOffset += MIN_ELEMENT_SEPARATION + saveConfigButton.getWidth();
        ezConfigButton = new ExDepotImageButton(
                xOffset, yOffset, ExDepotImageButton.CHEST_AND_GEAR_BIDX,
                (button) -> {},
                new TranslatableComponent("exdepot.gui.depotrules.tooltip.frominventory"),
                this
        );
        xOffset += MIN_ELEMENT_SEPARATION + ezConfigButton.getWidth();
        allItemsToggle = new ExDepotImageButton(
                xOffset, yOffset, ExDepotImageButton.CHECKBOX_YES_ASTERISK_BIDX,
                (button) -> {},
                new TranslatableComponent("exdepot.gui.depotrules.tooltip.allitems"),
                this
        );
        xOffset = MIN_ELEMENT_SEPARATION;
        yOffset = MIN_ELEMENT_SEPARATION + ExDepotImageButton.BUTTON_HEIGHT + MIN_ELEMENT_SEPARATION;

        rulesBox = new RulesList(
                minecraft,
                width - 2 * MIN_ELEMENT_SEPARATION,
                height - MIN_ELEMENT_SEPARATION * 3 - ExDepotImageButton.BUTTON_HEIGHT,
                yOffset,
                xOffset,
                ExDepotImageButton.BUTTON_HEIGHT
        );
        rulesBox.dummyInit();

        this.addRenderableWidget(searchField);
        this.addRenderableWidget(clearConfigButton);
        this.addRenderableWidget(saveConfigButton);
        this.addRenderableWidget(ezConfigButton);
        this.addRenderableWidget(allItemsToggle);
        this.addRenderableWidget(rulesBox);
    }

    @Override
    public void tick() {
        this.hasUnsavedChanges = this.savedDepotRules != null && !this.savedDepotRules.equals(this.depotRules);
        this.searchField.tick();
    }

    @Override
    public boolean keyPressed(int key, int mouseX, int mouseY) {
        if (this.getFocused() != this.searchField || key != 257 && key != 335) {
            return super.keyPressed(key, mouseX, mouseY);
        } else {
            return true;
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicksProbably) {
        this.renderBackground(poseStack);
        for(Widget widget : this.renderables) {
            widget.render(poseStack, mouseX, mouseY, partialTicksProbably);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void renderBackground(PoseStack poseStack) {
        int upper_background_color = COLOR_BLACK_OPACITY_MEDIUM;
        int lower_background_color = COLOR_BLACK_OPACITY_HEAVY;
        if (this.hasUnsavedChanges) {
            lower_background_color = COLOR_DARK_GREEN_OPACITY_HEAVY;
        }
        this.fillGradient(poseStack, 0, 0, this.width, this.height, upper_background_color, lower_background_color);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.BackgroundDrawnEvent(this, poseStack));
    }

    private static int constructAlphaRGB(byte alpha, byte r, byte g, byte b) {
        return alpha<<24 | r<<16 | g<<8 | b;
    }
}
