package bike.guyona.exdepot.helpers;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Field;
import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.config.ExDepotConfig.forceCompatibility;

public class ModSupportHelpers {
    public static Vector<TileEntity> getInventories(Container container){
        Vector<TileEntity> tileEntities = new Vector<>();
        if (container != null && container instanceof ContainerChest){
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
        } else if (forceCompatibility && container != null) {
            Class clazz = container.getClass();
            TileEntity inventory = null;
            for (Field field :clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object tmpObject = null;
                try {
                    tmpObject = field.get(container);
                } catch (IllegalAccessException e) {
                    LOGGER.error("Apparently field %s on object %s is not actually in the object definition? " +
                            "Needless to say, this should be impossible.", field.toString(), container.toString());
                }
                if (tmpObject instanceof IInventory && tmpObject instanceof TileEntity) {
                    if (inventory == null) {
                        inventory = (TileEntity) tmpObject;
                    } else {
                        inventory = null;
                        break; // Only get invField if there's exactly one field of this type.
                    }
                }
            }
            if (inventory != null) {
                tileEntities.add(inventory);
            }
        }
        return tileEntities;
    }

    public static boolean isSupported(Container container) {
        return true;
    }

    public static boolean isSupported(TileEntity tileEntity) {
        return tileEntity instanceof IInventory;
    }

    public static boolean isSupported(IInventory inventory) {
        return inventory instanceof TileEntity;
    }

    public static boolean isSupported(GuiScreen gui) {
        return gui instanceof GuiContainer;
    }
}
