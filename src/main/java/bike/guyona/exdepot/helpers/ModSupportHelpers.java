package bike.guyona.exdepot.helpers;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.client.particles.ViewDepotParticle;
import com.mojang.math.Vector3d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 *
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

    /**
     * On a multiblock container (like a double chest), each part of that container copies its depot rules to each
     * other part.
     * This function gets those parts.
     *
     * For example if you attempt to store items to the left half of a double chest, obviously it should accept the
     * same items as the right half.
     * @param entity the BlockEntity that (may) be part of a larger multiblock.
     * @return
     */
    @NotNull
    public static List<BlockEntity> getBigDepot(BlockEntity entity) {
        if (!(entity instanceof ChestBlockEntity)) {
            return List.of(entity);
        }
        if (entity.getBlockState().getValue(BlockStateProperties.CHEST_TYPE) == ChestType.SINGLE) {
            return List.of(entity);
        }
        // It's a double chest
        List<BlockEntity> allEntities = new ArrayList<>();
        allEntities.add(entity);
        Direction otherChestDirection = ChestBlock.getConnectedDirection(entity.getBlockState());
        BlockPos otherChestLoc = new BlockPos(
                entity.getBlockPos().getX() + otherChestDirection.getStepX(),
                entity.getBlockPos().getY() + otherChestDirection.getStepY(),
                entity.getBlockPos().getZ() + otherChestDirection.getStepZ()
        );
        if (entity.getLevel() == null) {
            ExDepotMod.LOGGER.error("IMPOSSIBLE: BlockEntity is not in a level {}", entity);
            return allEntities;
        }
        allEntities.add(entity.getLevel().getBlockEntity(otherChestLoc));
        return allEntities;
    }

    /**
     * By default, Depot's configuration is displayed above the BlockEntity that has that Depot. However, there
     * are reasons to display the Depot configuration elsewhere. Perhaps a BlockEntity shares a Depot with
     * other BlockEntities, or maybe the BlockEntity emits particles that make it hard to see anything nearby it.
     * In either of these cases, you may want to display the Depot further away from the BlockEntity(s) that host it.
     * @param entity some BlockEntity that has a Depot.
     * @return the center of the ViewDepotParticle that shows the Depot for the passed entity.
     */
    public static Vector3d viewDepotDrawLocation(BlockEntity entity) {
        List<BlockEntity> allEntites = getBigDepot(entity);
        if (allEntites.size() == 0) {
            ExDepotMod.LOGGER.error("IMPOSSIBLE: a multiblock {} is composed of 0 BlockEntities.", entity);
            return new Vector3d(0,0,0);
        }
        double x = 0,y = Double.NEGATIVE_INFINITY,z = 0;
        for (BlockEntity part : allEntites) {
            x += part.getBlockPos().getX();
            y = Math.max(y, part.getBlockPos().getY());
            z += part.getBlockPos().getZ();
        }
        x /= allEntites.size();
        z /= allEntites.size();
        return new Vector3d(
                x + ViewDepotParticle.EAST_OFFSET,
                y + ViewDepotParticle.UP_OFFSET,
                z + ViewDepotParticle.NORTH_OFFSET
        );
    }

    /*
     * The above is an implementation for vanilla Minecraft.
     * should I continue creating implementations this way, or require that people implement my interface?
     * ExDepot side implementation:
     * * Completely under my control (don't need to get mod authors involved)
     * * Completely under my control (can change the API)
     * * Neither mod needs any additional jars.
     * ClientMod side implementation (interface style):
     * * We automatically get the correct implementation for the installed mod version.
     */
}
