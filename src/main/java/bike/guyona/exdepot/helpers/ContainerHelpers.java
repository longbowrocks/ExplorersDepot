package bike.guyona.exdepot.helpers;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;

import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

public class ContainerHelpers {
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
                LOGGER.info("That's weird. We have a GUI open for a "+containerChest.getLowerChestInventory().toString());
            }
        }
        return tileEntities;
    }
}
