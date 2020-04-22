package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.network.StorageConfigCreateFromChestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import static bike.guyona.exdepot.Ref.CHEST_AND_GEAR_BIDX;

public class FromInventoryButton extends GuiIconButton{
    private BlockPos chestPosition;

    public FromInventoryButton(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, "exdepot.tooltip.frominventory.def", "exdepot.tooltip.frominventory.adv", CHEST_AND_GEAR_BIDX);
    }

    public void setChestPosition(BlockPos chestPos) {
        chestPosition = chestPos;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int i, int j) {
        if (super.mousePressed(mc, i, j)) {
            ExDepotMod.NETWORK.sendToServer(new StorageConfigCreateFromChestMessage(chestPosition));
            return true;
        }else {
            return false;
        }
    }

    @Override
    public String getTooltip() {
        if (tooltipCache == null) {
            tooltipCache = new TextComponentTranslation(tooltip).getUnformattedText();
        }
        return tooltipCache;
    }

    @Override
    public String getLongTooltip() {
        if (longTooltipCache == null) {
            longTooltipCache = new TextComponentTranslation(longTooltip).getUnformattedText();
        }
        return longTooltipCache;
    }
}
