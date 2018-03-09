package bike.guyona.exdepot.helpers;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.EnumFacing;

import java.lang.reflect.Field;
import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.config.ExDepotConfig.forceCompatibility;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class ModSupportHelpers {
    public static final CreativeTabs[] DISALLOWED_CATEGORIES = {
            CreativeTabs.SEARCH,
            CreativeTabs.INVENTORY
    };

    public static Vector<TileEntity> getContainerTileEntities(Container container){
        Vector<TileEntity> tileEntities = new Vector<>();
        if (container instanceof ContainerChest){
            ContainerChest containerChest = (ContainerChest) container;
            if (containerChest.getLowerChestInventory() instanceof TileEntityChest) {
                tileEntities.add((TileEntity) containerChest.getLowerChestInventory());
            }else if (containerChest.getLowerChestInventory() instanceof InventoryLargeChest) {
                InventoryLargeChest largeChest = (InventoryLargeChest) containerChest.getLowerChestInventory();
                tileEntities.add((TileEntity) largeChest.upperChest);
                tileEntities.add((TileEntity) largeChest.lowerChest);
            }else {
                LOGGER.warn("That's weird. We have a GUI open for a "+
                        containerChest.getLowerChestInventory().toString());
            }
            return tileEntities;
        } else if (container instanceof ContainerShulkerBox) {
            TileEntity tileEntity = forceGetAttachedTileEntity(container);
            if (tileEntity != null) {
                tileEntities.add(tileEntity);
            }
        } else if (forceCompatibility && container != null) {
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
            if (tmpObject instanceof TileEntity && isTileEntitySupported((TileEntity) tmpObject)) {
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
        if (gui == null)
            return false;
        if (gui instanceof GuiChest ||
                gui instanceof GuiShulkerBox) {
            return true;
        } else if (forceCompatibility) {
            return gui instanceof GuiContainer;
        }
        return false;
    }

    public static boolean isContainerSupported(Container container) {
        if (container == null)
            return false;
        if (container instanceof ContainerChest ||
                container instanceof ContainerShulkerBox) {
            return true;
        } else if (forceCompatibility) {
            return true; // No defining characteristics on containers, so assume everything supported.
        }
        return false;
    }

    public static boolean isTileEntitySupported(TileEntity tileEntity) {
        if (tileEntity == null)
            return false;
        if (tileEntity instanceof TileEntityChest ||
                tileEntity instanceof TileEntityShulkerBox) {
            return true;
        } else if (forceCompatibility) {
            return tileEntity.hasCapability(ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        }
        return false;
    }

    /**
     * Can't check capabilities when adding capabilities, so check inheritance to give an estimate of whether this
     * entity will have an item handler.
     */
    public static boolean isTileEntitySupportedBestGuess(TileEntity tileEntity) {
        if (tileEntity == null)
            return false;
        if (tileEntity instanceof TileEntityChest ||
                tileEntity instanceof TileEntityShulkerBox) {
            return true;
        } else if (forceCompatibility) {
            return tileEntity instanceof IInventory;
        }
        return false;
    }
}
