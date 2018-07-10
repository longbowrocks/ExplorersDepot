package bike.guyona.exdepot.config.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.config.*;

import static net.minecraftforge.fml.client.config.GuiUtils.RESET_CHAR;
import static net.minecraftforge.fml.client.config.GuiUtils.UNDO_CHAR;

public class GuiEditExtendedArray extends GuiEditArray{
    private static final int WARNING_DISPLAY_PERIOD_NANO = 2 * 1000 * 1000 * 1000;
    private long lastInvalidWarning = 0;
    private String errorMsg;

    public GuiEditExtendedArray(GuiScreen parentScreen, IConfigElement configElement, int slotIndex, Object[] currentValues, boolean enabled) {
        super(parentScreen, configElement, slotIndex, currentValues, enabled);
        errorMsg = new TextComponentTranslation("exdepot.config.compatListEdit.error", configElement.getType().name()).getUnformattedText();
    }

    @Override
    public void initGui()
    {
        this.entryList = new GuiEditExtendedArrayEntries(this, this.mc, this.configElement, this.beforeValues, this.currentValues);

        int undoGlyphWidth = mc.fontRenderer.getStringWidth(UNDO_CHAR) * 2;
        int resetGlyphWidth = mc.fontRenderer.getStringWidth(RESET_CHAR) * 2;
        int doneWidth = Math.max(mc.fontRenderer.getStringWidth(I18n.format("gui.done")) + 20, 100);
        int undoWidth = mc.fontRenderer.getStringWidth(" " + I18n.format("fml.configgui.tooltip.undoChanges")) + undoGlyphWidth + 20;
        int resetWidth = mc.fontRenderer.getStringWidth(" " + I18n.format("fml.configgui.tooltip.resetToDefault")) + resetGlyphWidth + 20;
        int buttonWidthHalf = (doneWidth + 5 + undoWidth + 5 + resetWidth) / 2;
        this.buttonList.add(btnDone = new GuiButtonExt(2000, this.width / 2 - buttonWidthHalf, this.height - 29, doneWidth, 20, I18n.format("gui.done")));
        this.buttonList.add(btnDefault = new GuiUnicodeGlyphButton(2001, this.width / 2 - buttonWidthHalf + doneWidth + 5 + undoWidth + 5,
                this.height - 29, resetWidth, 20, " " + I18n.format("fml.configgui.tooltip.resetToDefault"), RESET_CHAR, 2.0F));
        this.buttonList.add(btnUndoChanges = new GuiUnicodeGlyphButton(2002, this.width / 2 - buttonWidthHalf + doneWidth + 5,
                this.height - 29, undoWidth, 20, " " + I18n.format("fml.configgui.tooltip.undoChanges"), UNDO_CHAR, 2.0F));
    }

    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        super.drawScreen(par1, par2, par3);
        if (System.nanoTime() - lastInvalidWarning < WARNING_DISPLAY_PERIOD_NANO) {
            int alpha = (int)(255 * (System.nanoTime() - lastInvalidWarning) / WARNING_DISPLAY_PERIOD_NANO);
            drawCenteredString(this.fontRenderer, errorMsg, this.width/2, this.height/2, getErrorColor(alpha));
        }
    }

    private int getErrorColor(int alpha) {
        int red = 255;
        int green = 0;
        int blue = 0;
        return (alpha << 24) + (red << 16) + (green << 8) + blue;
    }
}
