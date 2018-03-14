package bike.guyona.exdepot.gui.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

import static bike.guyona.exdepot.ExDepotMod.MOD_BUTTON_TEXTURES;
import static bike.guyona.exdepot.Ref.TOOLTIP_OFFSET;

public class GuiIconButton extends GuiButton {
    private static final int BUTTONS_PER_ROW = 12;
    private String tooltip;
    private String longTooltip;
    protected int buttonIndex;

    public GuiIconButton(int id_, int x, int y, int w, int h, String tooltip, String longTooltip, int buttonIndex) {
        super(id_, x, y, w, h, "");
        this.tooltip = tooltip;
        this.longTooltip = longTooltip;
        this.buttonIndex = buttonIndex;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(MOD_BUTTON_TEXTURES);
        drawTexturedModalRect(xPosition, yPosition, 20*(buttonIndex%BUTTONS_PER_ROW), 20*(buttonIndex/BUTTONS_PER_ROW), width, height);
    }

    public boolean containsClick(int mouseX, int mouseY) {
        return mouseX > xPosition && mouseX < xPosition + width &&
                mouseY > yPosition && mouseY < yPosition + height;
    }

    public void drawTooltip(int x, int y) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        int textWidth = fontRenderer.getStringWidth(tooltip);
        int textHeight = 11;
        drawGradientRect(x + TOOLTIP_OFFSET - 3,
                y - TOOLTIP_OFFSET - 3,
                x + TOOLTIP_OFFSET + textWidth + 3,
                y - TOOLTIP_OFFSET + textHeight + 3,
                0xc0000000,
                0xc0000000);
        fontRenderer.drawStringWithShadow(tooltip, x + TOOLTIP_OFFSET, y - TOOLTIP_OFFSET, -1);
    }
}
