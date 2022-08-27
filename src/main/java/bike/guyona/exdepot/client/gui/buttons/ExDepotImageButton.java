package bike.guyona.exdepot.client.gui.buttons;

import bike.guyona.exdepot.Ref;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

public class ExDepotImageButton extends ImageButton {
    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTON_WIDTH = 20;
    public static final int SPRITES_PER_ROW = 12;
    private static final int SPRITE_SHEET_SIZE = 256;

    public static final ResourceLocation SPRITE_SHEET = new ResourceLocation(Ref.MODID, "textures/gui/button_icons.png");
    public static final int CHECKBOX_NO_BIDX = 0;
    public static final int CHECKBOX_YES_BIDX = 1;
    public static final int CHECKBOX_NO_ASTERISK_BIDX = 2;
    public static final int CHECKBOX_YES_ASTERISK_BIDX = 3;
    public static final int GEAR_SMALL_BIDX = 4;
    public static final int CHEST_BIDX = 5;
    public static final int CHEST_AND_GEAR_BIDX = 6;
    public static final int FLOPPY_DISK_BIDX = 7;
    public static final int RED_X_BIDX = 8;
    public static final int NBT_NO_BIDX = 9;
    public static final int NBT_YES_BIDX = 10;
    public static final int QUESTION_MARK_NO_BIDX = 11;
    public static final int QUESTION_MARK_YES_BIDX = 12;
    public static final int BRAIN_AND_GEAR_BIDX = 13;
    public static final int GREEN_CHECK_SMALL_BIDX = 14;
    public static final int RED_X_SMALL_BIDX = 15;

    public ExDepotImageButton(int x, int y, int buttonIdx, Button.OnPress onPress, Component tooltip, Screen parentScreen) {
        // ImageButton(x, y, width, height, xTexStart, yTexStart, yAddOnHoverTex, imageLoc, texWidth, texHeight, onPress, onTooltip, tooltip)
        super(
                x, y, BUTTON_WIDTH, BUTTON_HEIGHT, getTexX(buttonIdx), getTexY(buttonIdx), 0, SPRITE_SHEET, SPRITE_SHEET_SIZE, SPRITE_SHEET_SIZE, onPress,
                tooltip == null ? Button.NO_TOOLTIP : new Button.OnTooltip() {
                    @ParametersAreNonnullByDefault
                    public void onTooltip(Button button, PoseStack poseStack, int mouseX, int mouseY) {
                        parentScreen.renderTooltip(poseStack, Minecraft.getInstance().font.split(tooltip, Math.max(parentScreen.width / 2 - 43, 170)), mouseX, mouseY);
                    }

                    @ParametersAreNonnullByDefault
                    public void narrateTooltip(Consumer<Component> narrator) {
                        narrator.accept(tooltip);
                    }
                },
                tooltip
        );
    }

    private static int getTexX(int buttonIdx) {
        return BUTTON_WIDTH * (buttonIdx % SPRITES_PER_ROW);
    }

    private static int getTexY(int buttonIdx) {
        return BUTTON_HEIGHT * (buttonIdx / SPRITES_PER_ROW);
    }

    @ParametersAreNonnullByDefault
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(poseStack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.blit(poseStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        this.renderBg(poseStack, minecraft, mouseX, mouseY);

        super.renderButton(poseStack, mouseX, mouseY, partialTicks);
    }
}
