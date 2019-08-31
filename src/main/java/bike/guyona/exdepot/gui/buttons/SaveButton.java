package bike.guyona.exdepot.gui.buttons;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.network.StorageConfigCreateMessage;
import bike.guyona.exdepot.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.Ref.FLOPPY_DISK_BIDX;

public class SaveButton extends GuiIconButton {
    private BlockPos chestPosition;

    public SaveButton(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, "exdepot.tooltip.save.def", "exdepot.tooltip.save.adv", FLOPPY_DISK_BIDX);
    }

    public void setChestPosition(BlockPos chestPos) {
        chestPosition = chestPos;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int i, int j) {
        if (super.mousePressed(mc, i, j)) {
            if(mc.world != null && mc.player != null) {
                if(mc.currentScreen != null && mc.currentScreen instanceof StorageConfigGui) {
                    StorageConfigGui confGui = (StorageConfigGui) mc.currentScreen;
                    ExDepotMod.NETWORK.sendToServer(new StorageConfigCreateMessage(confGui.getStorageConfig(), chestPosition));
                } else {
                    LOGGER.error("save screen is "+(mc.currentScreen == null ? "NULL" : mc.currentScreen.toString()));
                }
            }
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
