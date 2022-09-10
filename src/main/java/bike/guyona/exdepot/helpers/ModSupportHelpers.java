package bike.guyona.exdepot.helpers;

import net.minecraft.world.level.block.entity.*;

/**
 * Vanilla Supported:
 * Brewing Stand
 * Chest
 * Dispenser
 * Double Chest
 * Dropper
 * Furnace
 * Hopper
 * Shulker Box
 *
 * Vanilla Denied:
 * Beacon
 * Enchanting Table
 * Horse Chest
 * Workbench
 * Anvil
 * Ender Chest
 * Player Inventory
 */
public class ModSupportHelpers {
    /**
     * Possible implementations:
     * Enumerate all supported entities
     * Check for capabilities used by supported entities in each mod
     * Check for ItemHandler capability <- this may crash the game in unexpected ways
     * Check for
     * @param entity the entity we wish to verify can have a depot attached.
     * @return can the entity have a depot attached
     */
    public static boolean isBlockEntityCompatible(BlockEntity entity) {
        return entity instanceof ChestBlockEntity || // also catches TrappedChestBlockEntity
                entity instanceof HopperBlockEntity ||
                entity instanceof BrewingStandBlockEntity ||
                entity instanceof FurnaceBlockEntity ||
                entity instanceof BlastFurnaceBlockEntity ||
                entity instanceof SmokerBlockEntity ||
                entity instanceof JukeboxBlockEntity ||
                entity instanceof ShulkerBoxBlockEntity ||
                entity instanceof BarrelBlockEntity ||
                entity instanceof DispenserBlockEntity; // also catches DropperBlockEntity
                // ComposterBlock doesn't have a BlockEntity.
    }
}
