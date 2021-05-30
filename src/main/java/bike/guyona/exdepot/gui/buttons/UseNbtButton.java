package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.Ref.*;

public class UseNbtButton extends GuiIconButton {
    public UseNbtButton(int x, int y, int width, int height) {
        super(x, y, width, height, "exdepot.tooltip.usenbt.def", "exdepot.tooltip.usenbt.adv", NBT_YES_BIDX);
    }

    @Override
    public void onPress() {
        Minecraft mc = Minecraft.getInstance();
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
    }

    public void setToggle(boolean value) {
        buttonIndex = value ? NBT_YES_BIDX : NBT_NO_BIDX;
        tooltipCache = null;
        longTooltipCache = null;
    }

    @Override
    public String getTooltip() {
        if (tooltipCache == null) {
            tooltipCache = new TranslationTextComponent(tooltip,
                    buttonIndex == NBT_YES_BIDX ? TextFormatting.GREEN : TextFormatting.RED,
                    new TranslationTextComponent(buttonIndex == NBT_YES_BIDX ? "options.on" : "options.off").getUnformattedComponentText(),
                    TextFormatting.RESET).getUnformattedComponentText();
        }
        return tooltipCache;
    }

    @Override
    public String getLongTooltip() {
        if (longTooltipCache == null) {
            longTooltipCache = new TranslationTextComponent(longTooltip,
                    TextFormatting.GREEN,
                    TextFormatting.RESET).getUnformattedComponentText();
        }
        return longTooltipCache;
    }
}
