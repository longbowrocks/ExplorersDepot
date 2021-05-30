package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.Ref.CHECKBOX_NO_ASTERISK_BIDX;
import static bike.guyona.exdepot.Ref.CHECKBOX_YES_ASTERISK_BIDX;
import static bike.guyona.exdepot.Ref.QUESTION_MARK_YES_BIDX;

public class AllItemsButton extends GuiIconButton {
    public AllItemsButton(int x, int y, int width, int height) {
        super(x, y, width, height, "exdepot.tooltip.allitems.def", "exdepot.tooltip.allitems.adv", CHECKBOX_YES_ASTERISK_BIDX);
    }

    @Override
    public void onPress() {
        Minecraft mc = Minecraft.getInstance();
        if(mc.world != null && mc.player != null) {
            if(mc.currentScreen != null && mc.currentScreen instanceof StorageConfigGui) {
                StorageConfigGui confGui = (StorageConfigGui) mc.currentScreen;
                StorageConfig conf = confGui.getStorageConfig();
                conf.allItems = !conf.allItems;
                confGui.setStorageConfig(conf);
                setToggle(conf.allItems);
            } else {
                LOGGER.error("allitems screen is "+(mc.currentScreen == null ? "NULL" : mc.currentScreen.toString()));
            }
        }
    }

    public void setToggle(boolean value) {
        buttonIndex = value ? CHECKBOX_YES_ASTERISK_BIDX : CHECKBOX_NO_ASTERISK_BIDX;
        tooltipCache = null;
        longTooltipCache = null;
    }

    @Override
    public String getTooltip() {
        if (tooltipCache == null) {
            tooltipCache = new TranslationTextComponent(tooltip,
                    buttonIndex == CHECKBOX_YES_ASTERISK_BIDX ? TextFormatting.GREEN : TextFormatting.RED,
                    new TranslationTextComponent(buttonIndex == CHECKBOX_YES_ASTERISK_BIDX ? "options.on" : "options.off").getUnformattedComponentText(),
                    TextFormatting.RESET,
                    TextFormatting.YELLOW).getUnformattedComponentText();
        }
        return tooltipCache;
    }

    @Override
    public String getLongTooltip() {
        if (longTooltipCache == null) {
            longTooltipCache = new TranslationTextComponent(longTooltip,
                    TextFormatting.GREEN,
                    TextFormatting.RESET,
                    TextFormatting.YELLOW).getUnformattedComponentText();
        }
        return longTooltipCache;
    }
}
