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

import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;

public class StorageConfigCreateFromChestMessage implements IMessage {
    public StorageConfigCreateFromChestMessage(){}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    public static class StorageConfigCreateFromChestMessageHandler implements IMessageHandler<StorageConfigCreateFromChestMessage, IMessage> {
        @Override
        public IMessage onMessage(StorageConfigCreateFromChestMessage message, MessageContext ctx) {
            // This is the player the packet was sent to the server from
            EntityPlayerMP serverPlayer = ctx.getServerHandler().playerEntity;
            Vector<TileEntityChest> smallChests = new Vector<>();
            // Get chests being configured.
            if (serverPlayer.openContainer != null && serverPlayer.openContainer instanceof ContainerChest){
                ContainerChest containerChest = (ContainerChest) serverPlayer.openContainer;
                LOGGER.info("Config message should be associated with chest: "+containerChest.getLowerChestInventory().toString());
                if (containerChest.getLowerChestInventory() instanceof TileEntityChest) {
                    smallChests.add((TileEntityChest) containerChest.getLowerChestInventory());
                }else if (containerChest.getLowerChestInventory() instanceof InventoryLargeChest) {
                    InventoryLargeChest largeChest = (InventoryLargeChest) containerChest.getLowerChestInventory();
                    if (largeChest.upperChest instanceof TileEntityChest){
                        smallChests.add((TileEntityChest) largeChest.upperChest);
                    }
                    if (largeChest.lowerChest instanceof TileEntityChest){
                        smallChests.add((TileEntityChest) largeChest.lowerChest);
                    }
                }else {
                    LOGGER.info("That's weird. We have a GUI open for a " + containerChest.getLowerChestInventory().toString());
                }
            }
            // Associate chests with received StorageConfig, and add to cache.
            serverPlayer.getServerWorld().addScheduledTask(() -> {
                synchronized (proxy) {// Let's be real IntelliJ, you and I both know the proxy reference won't change.
                    StorageConfig storageConf = new StorageConfig();
                    for (TileEntityChest chest:smallChests) {
                        StorageConfig tmpConf = createConfFromChest(chest, serverPlayer.getServerWorld());
                        storageConf.allItems = storageConf.allItems || tmpConf.allItems;
                        storageConf.modIds.addAll(tmpConf.modIds);
                        storageConf.itemIds.addAll(tmpConf.itemIds);
                    }
                    ExDepotMod.NETWORK.sendTo(new StorageConfigCreateFromChestResponse(storageConf), serverPlayer);
                }
            });
            // No direct response packet
            return null;
        }

        private static StorageConfig createConfFromChest(TileEntityChest chest, WorldServer world) {
            StorageConfig config = new StorageConfig();
            BlockChest blockChest = (BlockChest) chest.getBlockType();
            IInventory chestInv = blockChest.getContainer(world, chest.getPos(), true);
            for (int chestInvIdx=0; chestInvIdx < chestInv.getSizeInventory(); chestInvIdx++) {
                ItemStack chestStack = chestInv.getStackInSlot(chestInvIdx);
                if (!chestStack.isEmpty()) {
                    config.itemIds.add(Item.REGISTRY.getIDForObject(chestStack.getItem()));
                }
            }
            return config;
        }
    }
}
