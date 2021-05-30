package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.helpers.GuiHelpers;
import bike.guyona.exdepot.network.StorageConfigRequestMessage;
import net.minecraft.util.text.TranslationTextComponent;

import static bike.guyona.exdepot.Ref.GEAR_SMALL_BIDX;


/**
 * Created by longb on 9/17/2017.
 */
public class StorageConfigButton extends GuiIconButton {
    public StorageConfigButton(int buttonId, int x, int y, int width, int height) {
        super(x, y, width, height, "exdepot.tooltip.opengui.def", "", GEAR_SMALL_BIDX);
    }

    @Override
    public void onPress() {
        ExDepotMod.NETWORK.sendToServer(new StorageConfigRequestMessage());
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        super.renderButton(mouseX, mouseY, partialTicks);
        if (containsClick(mouseX, mouseY)) {
            GuiHelpers.drawTooltip(this, mouseX, mouseY, false);
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
        return "";
    }
}
