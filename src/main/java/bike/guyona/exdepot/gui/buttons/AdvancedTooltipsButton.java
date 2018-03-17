package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.Ref.QUESTION_MARK_NO_BIDX;
import static bike.guyona.exdepot.Ref.QUESTION_MARK_YES_BIDX;

public class AdvancedTooltipsButton extends GuiIconButton {
    public AdvancedTooltipsButton(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, "exdepot.tooltip.showtooltips.def",
                "exdepot.tooltip.showtooltips.adv", QUESTION_MARK_NO_BIDX);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int i, int j) {
        if (super.mousePressed(mc, i, j)) {
            if(mc.world != null && mc.player != null) {
                if(mc.currentScreen != null && mc.currentScreen instanceof StorageConfigGui) {
                    StorageConfigGui confGui = (StorageConfigGui) mc.currentScreen;
                    boolean advancedTooltips = confGui.getShowAdvancedTooltips();
                    confGui.setShowAdvancedTooltips(!advancedTooltips);
                    setToggle(confGui.getShowAdvancedTooltips());
                } else {
                    LOGGER.error("help screen is "+(mc.currentScreen == null ? "NULL" : mc.currentScreen.toString()));
                }
            }
            return true;
        }else {
            return false;
        }
    }

    private void setToggle(boolean value) {
        buttonIndex = value ? QUESTION_MARK_YES_BIDX : QUESTION_MARK_NO_BIDX;
        tooltipCache = null;
        longTooltipCache = null;
    }

    @Override
    public String getTooltip() {
        if (tooltipCache == null) {
            tooltipCache = new TextComponentTranslation(tooltip).getUnformattedText();
        }
        return tooltipCache;
    }

    @Override
    public String getLongTooltip() {
        if (longTooltipCache == null) {
            longTooltipCache = new TextComponentTranslation(longTooltip).getUnformattedText();
        }
        return longTooltipCache;
    }
}
