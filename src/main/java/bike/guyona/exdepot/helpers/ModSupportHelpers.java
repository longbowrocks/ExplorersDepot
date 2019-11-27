package bike.guyona.exdepot.helpers;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.api.IExDepotContainer;
import bike.guyona.exdepot.api.IExDepotGui;
import bike.guyona.exdepot.api.IExDepotTileEntity;
import bike.guyona.exdepot.config.ExDepotConfig;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

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
    public static final CreativeTabs[] DISALLOWED_CATEGORIES = {
            CreativeTabs.SEARCH,
            CreativeTabs.INVENTORY
    };

    public static TileEntity getTileEntityFromBlockPos(BlockPos pos, World world) {
        if (pos.getY() < 0) {
            LOGGER.info("{} is not a good position", pos);
            return null;
        }
        TileEntity possibleChest = world.getTileEntity(pos);
        if (possibleChest == null) {
            LOGGER.info("{} has no chest", pos);
            return null;
        }
        return possibleChest;
    }

    // getItemHandler doesn't work before capabilities are added, so this returns true for a superset of the actual
    // supported TileEntities, allowing us to add the storageConfig capability.
    public static boolean isTileEntitySupported(TileEntity tileEntity, boolean canCheckCapabilities) {
        return true;
    }

    public static IItemHandler getItemHandler(TileEntity tileEntity) {
        if (tileEntity.hasCapability(ITEM_HANDLER_CAPABILITY, EnumFacing.UP)) {
            return tileEntity.getCapability(ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        } else {
            // Some people don't use capabilities. Shame.
            IItemHandler itemHandler = forceGetAttachedItemHandler(tileEntity);
            if (itemHandler != null){
                return itemHandler;
            }
        }
        return null;
    }

    private static IItemHandler forceGetAttachedItemHandler(TileEntity tileEntity) {
        Class clazz = tileEntity.getClass();
        IItemHandler itemHandler = null;
        for (Field field :clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object tmpObject = null;
            try {
                tmpObject = field.get(tileEntity);
            } catch (IllegalAccessException e) {
                LOGGER.error("Apparently field {} on object {} is not actually in the object definition? " +
                        "Needless to say, this should be impossible.", field, tileEntity);
            }
            if (tmpObject instanceof IItemHandler) {
                if (itemHandler == null) {
                    itemHandler = (IItemHandler) tmpObject;
                } else {
                    itemHandler = null;
                    break; // Only get invField if there's exactly one field that could be the chest.
                }
            }
        }
        return itemHandler;
    }
}
