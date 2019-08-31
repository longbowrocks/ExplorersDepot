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

    // essentially isContainerSupported(), but returning a populated/empty list instead of true/false.
    public static List<TileEntity> getContainerTileEntities(Container container){
        Vector<TileEntity> tileEntities = new Vector<>();
        if (container instanceof ContainerChest){
            ContainerChest containerChest = (ContainerChest) container;
            if (containerChest.getLowerChestInventory() instanceof TileEntityChest) {
                tileEntities.add((TileEntity) containerChest.getLowerChestInventory());
            }else if (containerChest.getLowerChestInventory() instanceof InventoryLargeChest) {
                InventoryLargeChest largeChest = (InventoryLargeChest) containerChest.getLowerChestInventory();
                tileEntities.add(AccessHelpers.getUpperChest(largeChest));
                tileEntities.add(AccessHelpers.getLowerChest(largeChest));
            }else {
                LOGGER.warn("That's weird. We have a GUI open for a "+
                        containerChest.getLowerChestInventory().toString());
            }
            return tileEntities;
        } else if (container instanceof ContainerDispenser ||
                container instanceof ContainerHopper ||
                container instanceof ContainerShulkerBox) {
            TileEntity tileEntity = forceGetAttachedTileEntity(container);
            if (tileEntity != null) {
                tileEntities.add(tileEntity);
            }
        } else if (container instanceof IExDepotContainer) {
            return ((IExDepotContainer) container).getTileEntities();
        } else if (container != null && (
                ExDepotConfig.compatibilityMode.equals(Ref.COMPAT_MODE_DISCOVER) ||
                ExDepotConfig.compatibilityMode.equals(Ref.COMPAT_MODE_MANUAL)
                )) {
            TileEntity tileEntity = forceGetAttachedTileEntity(container);
            if (tileEntity != null) {
                tileEntities.add(tileEntity);
            }
        }
        return tileEntities;
    }

    private static TileEntity forceGetAttachedTileEntity(Container container) {
        Class clazz = container.getClass();
        TileEntity tileEntity = null;
        for (Field field :clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object tmpObject = null;
            try {
                tmpObject = field.get(container);
            } catch (IllegalAccessException e) {
                LOGGER.error("Apparently field {} on object {} is not actually in the object definition? " +
                        "Needless to say, this should be impossible.", field, container);
            }
            if (tmpObject instanceof TileEntity && isTileEntitySupported((TileEntity) tmpObject, true)) {
                if (tileEntity == null) {
                    tileEntity = (TileEntity) tmpObject;
                } else {
                    tileEntity = null;
                    break; // Only get invField if there's exactly one field that could be the chest.
                }
            }
        }
        return tileEntity;
    }

    public static boolean isGuiSupported(GuiScreen gui) {
        if (gui == null ||
                gui instanceof GuiBeacon ||
                gui instanceof GuiEnchantment ||
                gui instanceof GuiScreenHorseInventory ||
                gui instanceof GuiCrafting ||
                gui instanceof GuiRepair ||
                // Best guess if ender chest. This may produce false positives, but at least it will always detect ender chests.
                gui instanceof GuiChest && !(AccessHelpers.getLowerChestInventory((GuiChest)gui) instanceof ContainerLocalMenu) ||
                gui instanceof GuiInventory) {
            return false;
        }
        switch (ExDepotConfig.compatibilityMode) {
            case Ref.COMPAT_MODE_VANILLA:
                if (gui instanceof GuiChest ||
                        gui instanceof GuiDispenser ||
                        gui instanceof GuiHopper ||
                        gui instanceof GuiShulkerBox ||
                        gui instanceof IExDepotGui) {
                    return true;
                }
                break;
            case Ref.COMPAT_MODE_DISCOVER:
                return gui instanceof GuiContainer;
            case Ref.COMPAT_MODE_MANUAL:
                boolean guiMatches = ExDepotConfig.compatListMatch(gui);
                if (guiMatches && ExDepotConfig.compatListType.equals(Ref.MANUAL_COMPAT_TYPE_WHITE) ||
                    !guiMatches && ExDepotConfig.compatListType.equals(Ref.MANUAL_COMPAT_TYPE_BLACK)) {
                    return true;
                }
                break;
        }
        return false;
    }

    public static boolean isTileEntitySupported(TileEntity tileEntity, boolean canCheckCapabilities) {
        return true;
//        if (tileEntity == null ||
//                tileEntity instanceof TileEntityBeacon ||
//                tileEntity instanceof TileEntityEnchantmentTable ||
//                tileEntity instanceof TileEntityEnderChest) {
//            return false;
//        }
//        switch (ExDepotConfig.compatibilityMode) {
//            case Ref.COMPAT_MODE_VANILLA:
//                if (tileEntity instanceof TileEntityChest ||
//                        tileEntity instanceof TileEntityDispenser ||
//                        tileEntity instanceof TileEntityHopper ||
//                        tileEntity instanceof TileEntityShulkerBox ||
//                        tileEntity instanceof IExDepotTileEntity) {
//                    return true;
//                }
//                break;
//            case Ref.COMPAT_MODE_DISCOVER:
//            case Ref.COMPAT_MODE_MANUAL:
//                if (canCheckCapabilities) {
//                    return tileEntity.hasCapability(ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
//                } else {
//                    return tileEntity instanceof IInventory;
//                }
//        }
//        return false;
    }
}
