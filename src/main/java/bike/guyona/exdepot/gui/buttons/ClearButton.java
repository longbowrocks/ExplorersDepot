package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TranslationTextComponent;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.Ref.NBT_YES_BIDX;
import static bike.guyona.exdepot.Ref.RED_X_BIDX;

public class ClearButton extends GuiIconButton {
    public ClearButton(int x, int y, int width, int height) {
        super(x, y, width, height, "exdepot.tooltip.clear.def", "exdepot.tooltip.clear.adv", RED_X_BIDX);
    }

    @Override
    public void onPress() {
        Minecraft mc = Minecraft.getInstance();
        if(mc.world != null && mc.player != null) {
            if(mc.currentScreen != null && mc.currentScreen instanceof StorageConfigGui) {
                StorageConfigGui confGui = (StorageConfigGui) mc.currentScreen;
                confGui.setStorageConfig(new StorageConfig());
            } else {
                LOGGER.error("clear screen is "+(mc.currentScreen == null ? "NULL" : mc.currentScreen.toString()));
            }
        }
    }

    @Override
    public String getTooltip() {
        if (tooltipCache == null) {
            tooltipCache = new TranslationTextComponent(tooltip).getUnformattedComponentText();
        }
        return tooltipCache;
    }

    @Override
    public String getLongTooltip() {
        if (longTooltipCache == null) {
            longTooltipCache = new TranslationTextComponent(longTooltip).getUnformattedComponentText();
        }
        return longTooltipCache;
    }
}
