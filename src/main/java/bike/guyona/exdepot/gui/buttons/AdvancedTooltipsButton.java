package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.Ref.QUESTION_MARK_NO_BIDX;
import static bike.guyona.exdepot.Ref.QUESTION_MARK_YES_BIDX;

public class AdvancedTooltipsButton extends GuiIconButton {
    public AdvancedTooltipsButton(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, "Help (extended tooltips)",
                "This tooltip is now technically longer. " +
                        "Most tooltips will be more informative when they're longer. " +
                        "This one, not so much. " +
                        "I just don't know why you need help on your help.", QUESTION_MARK_NO_BIDX);
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

    public void setToggle(boolean value) {
        buttonIndex = value ? QUESTION_MARK_YES_BIDX : QUESTION_MARK_NO_BIDX;
    }
}
