package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capability.StorageConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashSet;
import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;

public class StorageConfigCreateFromChestMessage implements IMessage, IMessageHandler<StorageConfigCreateFromChestMessage, IMessage> {
    public StorageConfigCreateFromChestMessage(){}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public IMessage onMessage(StorageConfigCreateFromChestMessage message, MessageContext ctx) {
        // This is the player the packet was sent to the server from
        EntityPlayerMP serverPlayer = ctx.getServerHandler().playerEntity;
        serverPlayer.getServerWorld().addScheduledTask(() -> {
            TileEntityChest smallChest = null;
            // Get chests being configured.
            if (serverPlayer.openContainer != null && serverPlayer.openContainer instanceof ContainerChest){
                ContainerChest containerChest = (ContainerChest) serverPlayer.openContainer;
                LOGGER.info("Config message should be associated with chest: "+containerChest.getLowerChestInventory().toString());
                if (containerChest.getLowerChestInventory() instanceof TileEntityChest) {
                    smallChest = (TileEntityChest) containerChest.getLowerChestInventory();
                }else if (containerChest.getLowerChestInventory() instanceof InventoryLargeChest) {
                    InventoryLargeChest largeChest = (InventoryLargeChest) containerChest.getLowerChestInventory();
                    smallChest = (TileEntityChest) largeChest.upperChest; // inventory contains entire large chest
                }else {
                    LOGGER.info("That's weird. We have a GUI open for a " + containerChest.getLowerChestInventory().toString());
                }
            }
            if (smallChest == null) {
                return;
            }
            // Associate chest with received StorageConfig, and add to cache.
            //noinspection SynchronizeOnNonFinalField
            synchronized (proxy) {
                StorageConfig storageConf = new StorageConfig();
                StorageConfig tmpConf = createConfFromChest(smallChest, serverPlayer.getServerWorld());
                storageConf.allItems = storageConf.allItems || tmpConf.allItems;
                storageConf.modIds.addAll(tmpConf.modIds);
                storageConf.itemIds.addAll(tmpConf.itemIds);
                ExDepotMod.NETWORK.sendTo(new StorageConfigCreateFromChestResponse(storageConf), serverPlayer);
            }
        });
        // No direct response packet
        return null;
    }

    private static StorageConfig createConfFromChest(TileEntityChest chest, WorldServer world) {
        StorageConfig config = new StorageConfig();
        HashSet<Integer> itemIds = new HashSet<>();
        BlockChest blockChest = (BlockChest) chest.getBlockType();
        IInventory chestInv = blockChest.getContainer(world, chest.getPos(), true);
        for (int chestInvIdx=0; chestInvIdx < chestInv.getSizeInventory(); chestInvIdx++) {
            ItemStack chestStack = chestInv.getStackInSlot(chestInvIdx);
            if (!chestStack.isEmpty()) {
                itemIds.add(Item.REGISTRY.getIDForObject(chestStack.getItem()));
            }
        }
        config.itemIds.addAll(itemIds);
        return config;
    }
}
