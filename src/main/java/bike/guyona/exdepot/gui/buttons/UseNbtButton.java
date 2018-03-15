package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.Ref.*;

public class UseNbtButton extends GuiIconButton {
    public UseNbtButton(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, "exdepot.tooltip.usenbt.def", "exdepot.tooltip.usenbt.adv", NBT_YES_BIDX);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int i, int j) {
        if (super.mousePressed(mc, i, j)) {
            if(mc.world != null && mc.player != null) {
                if(mc.currentScreen != null && mc.currentScreen instanceof StorageConfigGui) {
                    StorageConfigGui confGui = (StorageConfigGui) mc.currentScreen;
                    StorageConfig conf = confGui.getStorageConfig();
                    conf.setUseNbt(!conf.getUseNbt());
                    confGui.setStorageConfig(conf);
                    setToggle(conf.getUseNbt());
                } else {
                    LOGGER.error("usenbt screen is "+(mc.currentScreen == null ? "NULL" : mc.currentScreen.toString()));
                }
            }
            return true;
        }else {
            return false;
        }
    }

    public void setToggle(boolean value) {
        buttonIndex = value ? NBT_YES_BIDX : NBT_NO_BIDX;
        tooltipCache = null;
        longTooltipCache = null;
    }

    @Override
    String getTooltip() {
        if (tooltipCache == null) {
            tooltipCache = new TextComponentTranslation(tooltip,
                    buttonIndex == NBT_YES_BIDX ? TextFormatting.GREEN : TextFormatting.RED,
                    buttonIndex == NBT_YES_BIDX ? "ON" : "OFF",
                    TextFormatting.RESET).getUnformattedText();
        }
        return tooltipCache;
    }

    @Override
    String getLongTooltip() {
        if (longTooltipCache == null) {
            longTooltipCache = new TextComponentTranslation(longTooltip,
                    TextFormatting.GREEN,
                    TextFormatting.RESET).getUnformattedText();
        }
        return longTooltipCache;
    }
}
