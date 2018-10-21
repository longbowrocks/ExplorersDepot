package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.config.ExDepotConfig;
import bike.guyona.exdepot.helpers.GuiHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;

import static bike.guyona.exdepot.Ref.GREEN_CHECK_SMALL_BIDX;
import static bike.guyona.exdepot.Ref.RED_X_SMALL_BIDX;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.isGuiSupported;

public class IngameConfigButton extends GuiIconButton {
    private GuiContainer parentGui;

    public IngameConfigButton(int buttonId, GuiContainer guiChest, int x, int y, int width, int height) {
        super(buttonId, x, y, width, height, "exdepot.tooltip.ingameconf.def", "", RED_X_SMALL_BIDX);
        parentGui = guiChest;
        setToggle(isGuiSupported(parentGui));
    }

    private void setToggle(boolean value) {
        buttonIndex = value ? GREEN_CHECK_SMALL_BIDX : RED_X_SMALL_BIDX;
        tooltipCache = null;
        longTooltipCache = null;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int i, int j) {
        if (super.mousePressed(mc, i, j)) {
            ExDepotConfig.addOrRemoveFromCompatList(parentGui);
            setToggle(isGuiSupported(parentGui));
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void drawButton(@NotNull Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
        if (containsClick(mouseX, mouseY)) {
            GuiHelpers.drawTooltip(this, mouseX, mouseY, false);
        }
    }

    @Override
    public String getTooltip() {
        if (tooltipCache == null) {
            tooltipCache = new TextComponentTranslation(tooltip,
                    buttonIndex == GREEN_CHECK_SMALL_BIDX ? TextFormatting.GREEN : TextFormatting.RED,
                    new TextComponentTranslation(buttonIndex == GREEN_CHECK_SMALL_BIDX ? "options.on" : "options.off").getUnformattedText(),
                    TextFormatting.RESET).getUnformattedText();
        }
        return tooltipCache;
    }

    @Override
    public String getLongTooltip() {
        return "";
    }
}
