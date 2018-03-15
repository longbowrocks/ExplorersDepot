package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.Ref.NBT_YES_BIDX;
import static bike.guyona.exdepot.Ref.RED_X_BIDX;

public class ClearButton extends GuiIconButton {
    public ClearButton(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, "exdepot.tooltip.clear.def", "exdepot.tooltip.clear.adv", RED_X_BIDX);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int i, int j) {
        if (super.mousePressed(mc, i, j)) {
            if(mc.world != null && mc.player != null) {
                if(mc.currentScreen != null && mc.currentScreen instanceof StorageConfigGui) {
                    StorageConfigGui confGui = (StorageConfigGui) mc.currentScreen;
                    confGui.setStorageConfig(new StorageConfig());
                } else {
                    LOGGER.error("clear screen is "+(mc.currentScreen == null ? "NULL" : mc.currentScreen.toString()));
                }
            }
            return true;
        }else {
            return false;
        }
    }

    @Override
    String getTooltip() {
        if (tooltipCache == null) {
            tooltipCache = new TextComponentTranslation(tooltip).getUnformattedText();
        }
        return tooltipCache;
    }

    @Override
    String getLongTooltip() {
        if (longTooltipCache == null) {
            longTooltipCache = new TextComponentTranslation(longTooltip).getUnformattedText();
        }
        return longTooltipCache;
    }
}
