package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.network.StorageConfigCreateFromChestMessage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import static bike.guyona.exdepot.Ref.CHEST_AND_GEAR_BIDX;

public class FromInventoryButton extends GuiIconButton{
    private BlockPos chestPosition;

    public FromInventoryButton(int x, int y, int width, int height) {
        super(x, y, width, height, "exdepot.tooltip.frominventory.def", "exdepot.tooltip.frominventory.adv", CHEST_AND_GEAR_BIDX);
    }

    public void setChestPosition(BlockPos chestPos) {
        chestPosition = chestPos;
    }

    @Override
    public void onPress() {
        ExDepotMod.NETWORK.sendToServer(new StorageConfigCreateFromChestMessage(chestPosition));
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
