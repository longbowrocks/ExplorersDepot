package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.config.ExDepotConfig;
import bike.guyona.exdepot.helpers.GuiHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            List<String> editArr = new ArrayList<>(Arrays.asList(ExDepotConfig.getCompatList()));
            String name = parentGui.getClass().getName();
            if (ExDepotConfig.compatListType.equals(Ref.COMPAT_MAN_TYPE_WHITE)) {
                if (ExDepotConfig.compatListMatch(parentGui)) {
                    editArr.remove(name);
                } else {
                    editArr.add(name);
                }
            } else {
                if (ExDepotConfig.compatListMatch(parentGui)) {
                    editArr.add(name);
                } else {
                    editArr.remove(name);
                }
            }
            ExDepotConfig.setCompatList(editArr.toArray(new String[0]));
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
