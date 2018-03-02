package bike.guyona.exdepot.network;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.helpers.TrackableItemStack;
import bike.guyona.exdepot.config.ExDepotConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.capability.StorageConfigProvider.STORAGE_CONFIG_CAPABILITY;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.isTileEntitySupported;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

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
                Collection<TileEntity> entities = player.getServerWorld().getChunkFromChunkCoords(chunkX, chunkZ).getTileEntityMap().values();
                for (TileEntity entity:entities) {
                    if (isTileEntitySupported(entity)){
                        BlockPos chestPos = entity.getPos();
                        if (player.getPosition().getDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) <
                                ExDepotConfig.storeRange &&
                                entity.getCapability(STORAGE_CONFIG_CAPABILITY, null) != null) {
                            chests.add(entity);
                        }
                    }
                }
            }
        }
        return chests;
    }

    private static Map<String, Integer> sortInventory(EntityPlayerMP player, Vector<TileEntity> chests){
        chests.sort((TileEntity o1, TileEntity o2) -> {
                BlockPos pos1 = o1.getPos();
                BlockPos pos2 = o2.getPos();
                if (pos1.getY() != pos2.getY()) {
                    return Integer.compare(pos1.getY(), pos2.getY());
                }else if (pos1.getX() != pos2.getX()) {
                    return Integer.compare(pos1.getX(), pos2.getX());
                }else {
                    return Integer.compare(pos1.getZ(), pos2.getZ());
                }
            }
        );
        TreeMap<TrackableItemStack, Vector<TileEntity>> itemMap = getItemMap(chests);
        HashMap<String, Vector<TileEntity>> modMap = getModMap(chests);
        Vector<TileEntity> allItemsList = itemMatchPriThree(chests);

        Set<BlockPos> chestsUsed = new HashSet<>();
        Integer itemsStored = 0;
        // indexes start in hotbar, move top left to bottom right through main inventory, then go to armor, then offhand slot.
        for (int i = InventoryPlayer.getHotbarSize(); i < player.inventory.mainInventory.size(); i++) {
            ItemStack istack = player.inventory.getStackInSlot(i);
            if (istack.isEmpty()) {
                continue;
            }
            Vector<TileEntity> itemIdChests = itemMatchPriOne(new TrackableItemStack(istack), itemMap);
            for (TileEntity chest:itemIdChests) {
                LOGGER.debug("Transferring by itemId at: {}", chest.getPos().toString());
                istack = transferItemStack(player, i, chest);
                if (istack.getCount() != player.inventory.getStackInSlot(i).getCount()) {
                    chestsUsed.add(chest.getPos());
                    itemsStored += player.inventory.getStackInSlot(i).getCount() - istack.getCount();
                }
                player.inventory.setInventorySlotContents(i, istack);
                player.inventory.markDirty();
                if (istack.isEmpty())
                    break;
            }
            if (istack.isEmpty())
                continue;
            Vector<TileEntity> modIdChests = itemMatchPriTwo(istack, modMap);
            for (TileEntity chest:modIdChests) {
                LOGGER.debug("Transferring by modId at: {}", chest.getPos().toString());
                istack = transferItemStack(player, i, chest);
                if (istack.getCount() != player.inventory.getStackInSlot(i).getCount()) {
                    chestsUsed.add(chest.getPos());
                    itemsStored += istack.getCount() - player.inventory.getStackInSlot(i).getCount();
                }
                player.inventory.setInventorySlotContents(i, istack);
                player.inventory.markDirty();
                if (istack.isEmpty())
                    break;
            }
            if (istack.isEmpty())
                continue;
            for (TileEntity chest:allItemsList) {
                LOGGER.debug("Transferring by allItems at: {}", chest.getPos().toString());
                istack = transferItemStack(player, i, chest);
                if (istack.getCount() != player.inventory.getStackInSlot(i).getCount()) {
                    chestsUsed.add(chest.getPos());
                    itemsStored += istack.getCount() - player.inventory.getStackInSlot(i).getCount();
                }
                player.inventory.setInventorySlotContents(i, istack);
                player.inventory.markDirty();
                if (istack.isEmpty())
                    break;
            }
        }
        Map<String, Integer> sortStats = new HashMap<>();
        sortStats.put("ItemsStored", itemsStored);
        sortStats.put("ChestsStoredTo", chestsUsed.size());
        return sortStats;
    }

    private static TreeMap<TrackableItemStack, Vector<TileEntity>> getItemMap(Vector<TileEntity> chests) {
        TreeMap<TrackableItemStack, Vector<TileEntity>> itemMap = new TreeMap<>();
        for (TileEntity chest:chests) {
            StorageConfig config = chest.getCapability(STORAGE_CONFIG_CAPABILITY, null);
            if (config.itemIds.size() > 0) {
                for (TrackableItemStack stack:config.itemIds) {
                    itemMap.computeIfAbsent(stack, (k) -> new Vector<>());
                    itemMap.get(stack).add(chest);
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
    private static Vector<TileEntity> itemMatchPriOne(TrackableItemStack istack, TreeMap<TrackableItemStack, Vector<TileEntity>> itemMap) {
        if (itemMap.containsKey(istack)) {
            return itemMap.get(istack);
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
        IItemHandler itemHandler = chest.getCapability(ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        ItemStack playerStack = player.inventory.getStackInSlot(playerInvIdx);
        if (itemHandler == null) {
            LOGGER.error("This chest doesn't have an item handler, but it should");
            return playerStack;
        }
        // First try to stack with existing items.
        for (int chestInvIdx=0; chestInvIdx < itemHandler.getSlots(); chestInvIdx++) {
            ItemStack chestStack = itemHandler.getStackInSlot(chestInvIdx);
            if (canCombine(chestStack, playerStack)) {
                playerStack = itemHandler.insertItem(chestInvIdx, playerStack, false);
            }
            if (playerStack.isEmpty()) {
                return playerStack;
            }
        }
        // Then try making new stacks.
        for (int chestInvIdx=0; chestInvIdx < itemHandler.getSlots(); chestInvIdx++) {
            ItemStack chestStack = itemHandler.getStackInSlot(chestInvIdx);
            if (chestStack.isEmpty()) {
                playerStack = itemHandler.insertItem(chestInvIdx, playerStack, false);
            }
            if (playerStack.isEmpty()) {
                return playerStack;
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
            Map<String, Integer> sortStats = sortInventory(serverPlayer, nearbyChests);
            final long endTime = System.nanoTime();
            serverPlayer.sendMessage(
                    new TextComponentString(
                            String.format("Stored %d items to %d chest%s",
                                    sortStats.get("ItemsStored"),
                                    sortStats.get("ChestsStoredTo"),
                                    sortStats.get("ChestsStoredTo")==1?"":"s")
                    )
            );
            LOGGER.info("Storing items took "+(endTime-startTime)/1000000.0+" milliseconds");
        });
        // No response packet
        return null;
    }
}
