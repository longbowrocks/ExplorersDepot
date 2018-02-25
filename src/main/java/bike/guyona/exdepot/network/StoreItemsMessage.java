package bike.guyona.exdepot.network;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.config.ExDepotConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.capability.StorageConfigProvider.STORAGE_CONFIG_CAPABILITY;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.isSupported;

/**
 * Created by longb on 11/21/2017.
 */
public class StoreItemsMessage implements IMessage, IMessageHandler<StoreItemsMessage, IMessage> {
    public StoreItemsMessage(){}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    private static Vector<TileEntity> getLocalChests(EntityPlayerMP player){
        Vector<TileEntity> chests = new Vector<>();
        int chunkDist = (ExDepotConfig.storeRange >> 4) + 1;
        LOGGER.info(String.format("Storage range is %d blocks, or %d chunks", ExDepotConfig.storeRange, chunkDist));
        for (int chunkX = player.chunkCoordX-chunkDist; chunkX <= player.chunkCoordX+chunkDist; chunkX++) {
            for (int chunkZ = player.chunkCoordZ-chunkDist; chunkZ <= player.chunkCoordZ+chunkDist; chunkZ++) {
                Collection<TileEntity> entitites = player.getServerWorld().getChunkFromChunkCoords(chunkX, chunkZ).getTileEntityMap().values();
                for (TileEntity entity:entitites) {
                    if (isSupported(entity)){
                        BlockPos chestPos = entity.getPos();
                        if (player.getPosition().getDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) <
                                ExDepotConfig.storeRange &&
                                entity.getCapability(STORAGE_CONFIG_CAPABILITY, null) != null) {
                            chests.add((TileEntity) entity);
                        }
                    }
                }
            }
        }
        return chests;
    }

    private static void sortInventory(EntityPlayerMP player, Vector<TileEntity> chests){
        chests.sort((TileEntity o1, TileEntity o2) -> {
                BlockPos pos1 = o1.getPos();
                BlockPos pos2 = o2.getPos();
                if (pos1.getX() != pos2.getX()) {
                    return Integer.compare(pos1.getX(), pos2.getX());
                }else if (pos1.getY() != pos2.getY()) {
                    return Integer.compare(pos1.getY(), pos2.getY());
                }else {
                    return Integer.compare(pos1.getZ(), pos2.getZ());
                }
            }
        );
        TreeMap<Integer, Vector<TileEntity>> itemMap = getItemMap(chests);
        HashMap<String, Vector<TileEntity>> modMap = getModMap(chests);
        Vector<TileEntity> allItemsList = itemMatchPriThree(chests);

        // indexes start in hotbar, move top left to bottom right through main inventory, then go to armor, then offhand slot.
        for (int i = InventoryPlayer.getHotbarSize(); i < player.inventory.mainInventory.size(); i++) {
            ItemStack istack = player.inventory.getStackInSlot(i);
            if (istack.isEmpty()) {
                continue;
            }
            Vector<TileEntity> itemIdChests = itemMatchPriOne(istack, itemMap);
            for (TileEntity chest:itemIdChests) {
                LOGGER.debug("Transferring by itemId at: " + chest.getPos().toString());
                istack = transferItemStack(player, i, chest);
                player.inventory.setInventorySlotContents(i, istack);
                player.inventory.markDirty();
                if (istack.isEmpty())
                    break;
            }
            if (istack.isEmpty())
                continue;
            Vector<TileEntity> modIdChests = itemMatchPriTwo(istack, modMap);
            for (TileEntity chest:modIdChests) {
                LOGGER.debug("Transferring by modId at: " + chest.getPos().toString());
                istack = transferItemStack(player, i, chest);
                player.inventory.setInventorySlotContents(i, istack);
                player.inventory.markDirty();
                if (istack.isEmpty())
                    break;
            }
            if (istack.isEmpty())
                continue;
            for (TileEntity chest:allItemsList) {
                LOGGER.debug("Transferring by allItems at: " + chest.getPos().toString());
                istack = transferItemStack(player, i, chest);
                player.inventory.setInventorySlotContents(i, istack);
                player.inventory.markDirty();
                if (istack.isEmpty())
                    break;
            }
        }
    }

    private static TreeMap<Integer, Vector<TileEntity>> getItemMap(Vector<TileEntity> chests) {
        TreeMap<Integer, Vector<TileEntity>> itemMap = new TreeMap<>();
        for (TileEntity chest:chests) {
            StorageConfig config = chest.getCapability(STORAGE_CONFIG_CAPABILITY, null);
            if (config.itemIds.size() > 0) {
                for (int itemId:config.itemIds) {
                    itemMap.computeIfAbsent(itemId, (k) -> new Vector<>());
                    itemMap.get(itemId).add(chest);
                }
            }
        }
        return itemMap;
    }

    private static HashMap<String, Vector<TileEntity>> getModMap(Vector<TileEntity> chests) {
        HashMap<String, Vector<TileEntity>> modMap = new HashMap<>();
        for (TileEntity chest:chests) {
            StorageConfig config = chest.getCapability(STORAGE_CONFIG_CAPABILITY, null);
            if (config.modIds.size() > 0) {
                for (String modId:config.modIds) {
                    modMap.computeIfAbsent(modId, (k) -> new Vector<>());
                    modMap.get(modId).add(chest);
                }
            }
        }
        return modMap;
    }

    // itemId match
    private static Vector<TileEntity> itemMatchPriOne(ItemStack istack, TreeMap<Integer, Vector<TileEntity>> itemMap) {
        int itemId = Item.REGISTRY.getIDForObject(istack.getItem());
        if (itemMap.containsKey(itemId)) {
            return itemMap.get(itemId);
        }
        return new Vector<>();
    }

    // mod match
    private static Vector<TileEntity> itemMatchPriTwo(ItemStack istack, HashMap<String, Vector<TileEntity>> modMap) {
        String modId = istack.getItem().getRegistryName().getResourceDomain();
        if (modMap.containsKey(modId)) {
            return modMap.get(modId);
        }
        return new Vector<>();
    }

    // allItems match
    private static Vector<TileEntity> itemMatchPriThree(Vector<TileEntity> chests) {
        Vector<TileEntity> allItemsList = new Vector<>();
        for (TileEntity chest:chests) {
            StorageConfig config = chest.getCapability(STORAGE_CONFIG_CAPABILITY, null);
            if (config.allItems) {
                allItemsList.add(chest);
            }
        }
        return allItemsList;
    }

    // Wow, how was there no helper method for this? What's next? No helper for MINE-ing blocks or CRAFTing items?
    private static ItemStack transferItemStack(EntityPlayerMP player, int playerInvIdx, TileEntity chest){
        BlockChest blockChest = (BlockChest) chest.getBlockType();
        IInventory chestInv = blockChest.getContainer(player.getServerWorld(), chest.getPos(), true);
        ItemStack playerStack = player.inventory.getStackInSlot(playerInvIdx);
        for (int chestInvIdx=0; chestInvIdx < chestInv.getSizeInventory(); chestInvIdx++) {
            ItemStack chestStack = chestInv.getStackInSlot(chestInvIdx);
            if (chestStack.isEmpty()) {
                // Careful: chest now owns this stack object. Give player a new one through return value.
                chestInv.setInventorySlotContents(chestInvIdx, playerStack);
                playerStack = ItemStack.EMPTY;
                chestInv.markDirty();
                break;
            } else if (canCombine(chestStack, playerStack)) {
                int maxChestCanAccept = playerStack.getMaxStackSize() - chestStack.getCount();
                int numberToTransfer = Math.min(playerStack.getCount(), maxChestCanAccept);
                chestStack.grow(numberToTransfer);
                playerStack.shrink(numberToTransfer);
                chestInv.markDirty();
            }
        }
        return playerStack;
    }

    private static boolean canCombine(ItemStack tgtStack, ItemStack srcStack)
    {
        return  tgtStack.getItem() == srcStack.getItem() &&
                tgtStack.getMetadata() == srcStack.getMetadata() &&
                tgtStack.getCount() <= tgtStack.getMaxStackSize() &&
                ItemStack.areItemStackTagsEqual(tgtStack, srcStack);
    }

    @Override
    public IMessage onMessage(StoreItemsMessage message, MessageContext ctx) {
        EntityPlayerMP serverPlayer = ctx.getServerHandler().playerEntity;
        serverPlayer.getServerWorld().addScheduledTask(() -> {
            final long startTime = System.nanoTime();
            Vector<TileEntity> nearbyChests = getLocalChests(serverPlayer);
            sortInventory(serverPlayer, nearbyChests);
            final long endTime = System.nanoTime();
            LOGGER.info("Storing items took "+(endTime-startTime)/1000000.0+" milliseconds");
        });
        // No response packet
        return null;
    }
}
