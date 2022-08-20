package bike.guyona.exdepot.helpers;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public enum ChestFullness {
    EMPTY,
    FILLING,
    FULL;

    /**
     * @param chest
     * @return fullness. A chest can have room (EMPTY), have 80% slots full (FILLING), or have 100% slots full (FULL)
     */
    public static ChestFullness getChestFullness(BlockEntity chest) {
        LazyOptional<IItemHandler> lazyItemHandler = chest.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
        if (!lazyItemHandler.isPresent()) {
            return ChestFullness.EMPTY;
        }
        IItemHandler itemHandler = lazyItemHandler.orElse(null);
        int filledSlots = 0;
        for (int i=0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty())
                filledSlots += 1;
        }
        float pctFull = (float) filledSlots / (float) itemHandler.getSlots();
        if (pctFull < 0.8) {
            return ChestFullness.EMPTY;
        } else if (pctFull < 1) {
            return ChestFullness.FILLING;
        }
        return ChestFullness.FULL;
    }
}
