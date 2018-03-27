package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.gui.interfaces.IHasTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentBase;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.MOD_BUTTON_TEXTURES;
import static bike.guyona.exdepot.Ref.TOOLTIP_OFFSET;

public abstract class GuiIconButton extends GuiButton implements IHasTooltip {
    private static final int BUTTONS_PER_ROW = 12;
    String tooltip;
    String longTooltip;
    String tooltipCache;
    String longTooltipCache;
    int buttonIndex;

    GuiIconButton(int id_, int x, int y, int w, int h, String tooltip, String longTooltip, int buttonIndex) {
        super(id_, x, y, w, h, "");
        this.tooltip = tooltip;
        this.longTooltip = longTooltip;
        this.buttonIndex = buttonIndex;
    }

    public abstract String getTooltip();

    public abstract String getLongTooltip();

    @Override
    public void drawButton(@NotNull Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(MOD_BUTTON_TEXTURES);
        drawTexturedModalRect(x, y, 20*(buttonIndex%BUTTONS_PER_ROW), 20*(buttonIndex/BUTTONS_PER_ROW), width, height);
    }

    @Override
    public boolean containsClick(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width &&
                mouseY > y && mouseY < y + height;
    }
}
