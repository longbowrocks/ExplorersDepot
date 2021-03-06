package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.config.ExDepotConfig;
import bike.guyona.exdepot.sortingrules.*;
import bike.guyona.exdepot.sortingrules.item.ItemSortingRule;
import bike.guyona.exdepot.sortingrules.itemcategory.ItemCategorySortingRule;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import bike.guyona.exdepot.sortingrules.modwithitemcategory.ModWithItemCategorySortingRule;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;
import static bike.guyona.exdepot.capability.StorageConfigProvider.STORAGE_CONFIG_CAPABILITY;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.getItemHandler;
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
        LOGGER.info("Storage range is {} blocks, or {} chunks", ExDepotConfig.storeRange, chunkDist);
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

    private static Map<String, Integer> sortInventory(EntityPlayerMP player, Vector<TileEntity> chests, Map<BlockPos, List<ItemStack>> sortingResultsOut){
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
        Map<ItemSortingRule, Vector<TileEntity>> itemMap = getItemMap(chests);
        Map<ModWithItemCategorySortingRule, Vector<TileEntity>> modWithItemCatMap = getModWithItemCategoryMap(chests);
        Map<ItemCategorySortingRule, Vector<TileEntity>> itemCategoryMap = getItemCategoryMap(chests);
        Map<ModSortingRule, Vector<TileEntity>> modMap = getModMap(chests);
        Vector<TileEntity> allItemsList = itemMatchPriFive(chests);

        Set<BlockPos> chestsUsed = new HashSet<>();
        Integer itemsStored = 0;
        // indexes start in hotbar, move top left to bottom right through main inventory, then go to armor, then offhand slot.
        for (int i = InventoryPlayer.getHotbarSize(); i < player.inventory.mainInventory.size(); i++) {
            ItemStack istack = player.inventory.getStackInSlot(i);
            if (istack.isEmpty()) {
                continue;
            }
            ItemStack stackCache = istack; // istack becomes air if it's emptied by a transfer.
            Vector<TileEntity> itemIdChests = itemMatchPriOne(istack, itemMap);
            for (TileEntity chest:itemIdChests) {
                LOGGER.debug("Transferring by itemId at: {}", chest.getPos().toString());
                istack = transferItemStack(player, i, chest);
                if (istack.getCount() != player.inventory.getStackInSlot(i).getCount()) {
                    chestsUsed.add(chest.getPos());
                    itemsStored += player.inventory.getStackInSlot(i).getCount() - istack.getCount();
                    recordItemTransfer(stackCache, itemsStored, chest, sortingResultsOut);
                }
                player.inventory.setInventorySlotContents(i, istack);
                player.inventory.markDirty();
                if (istack.isEmpty())
                    break;
            }
            if (istack.isEmpty())
                continue;
            Vector<TileEntity> modWithCatChests = itemMatchPriTwo(istack, modWithItemCatMap);
            for (TileEntity chest:modWithCatChests) {
                LOGGER.debug("Transferring by mod + item category at: {}", chest.getPos().toString());
                istack = transferItemStack(player, i, chest);
                if (istack.getCount() != player.inventory.getStackInSlot(i).getCount()) {
                    chestsUsed.add(chest.getPos());
                    itemsStored += player.inventory.getStackInSlot(i).getCount() - istack.getCount();
                    recordItemTransfer(stackCache, itemsStored, chest, sortingResultsOut);
                }
                player.inventory.setInventorySlotContents(i, istack);
                player.inventory.markDirty();
                if (istack.isEmpty())
                    break;
            }
            if (istack.isEmpty())
                continue;
            Vector<TileEntity> itemCatChests = itemMatchPriThree(istack, itemCategoryMap);
            for (TileEntity chest:itemCatChests) {
                LOGGER.debug("Transferring by item category at: {}", chest.getPos().toString());
                istack = transferItemStack(player, i, chest);
                if (istack.getCount() != player.inventory.getStackInSlot(i).getCount()) {
                    chestsUsed.add(chest.getPos());
                    itemsStored +=  player.inventory.getStackInSlot(i).getCount() - istack.getCount();
                    recordItemTransfer(stackCache, itemsStored, chest, sortingResultsOut);
                }
                player.inventory.setInventorySlotContents(i, istack);
                player.inventory.markDirty();
                if (istack.isEmpty())
                    break;
            }
            if (istack.isEmpty())
                continue;
            Vector<TileEntity> modIdChests = itemMatchPriFour(istack, modMap);
            for (TileEntity chest:modIdChests) {
                LOGGER.debug("Transferring by modId at: {}", chest.getPos().toString());
                istack = transferItemStack(player, i, chest);
                if (istack.getCount() != player.inventory.getStackInSlot(i).getCount()) {
                    chestsUsed.add(chest.getPos());
                    itemsStored += player.inventory.getStackInSlot(i).getCount() - istack.getCount();
                    recordItemTransfer(stackCache, itemsStored, chest, sortingResultsOut);
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
                    itemsStored += player.inventory.getStackInSlot(i).getCount() - istack.getCount();
                    recordItemTransfer(stackCache, itemsStored, chest, sortingResultsOut);
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

    private static Map<ItemSortingRule, Vector<TileEntity>> getItemMap(Vector<TileEntity> chests) {
        Map<ItemSortingRule, Vector<TileEntity>> itemMap = new HashMap<>();
        for (TileEntity chest:chests) {
            StorageConfig config = chest.getCapability(STORAGE_CONFIG_CAPABILITY, null);
            Set<? extends AbstractSortingRule> rules = config.getRules(ItemSortingRule.class);
            if (rules != null && rules.size() > 0) {
                for (AbstractSortingRule rule : rules) {
                    itemMap.computeIfAbsent((ItemSortingRule) rule, (k) -> new Vector<>());
                    itemMap.get(rule).add(chest);
                }
            }
        }
        return itemMap;
    }

    private static Map<ModWithItemCategorySortingRule, Vector<TileEntity>> getModWithItemCategoryMap(Vector<TileEntity> chests) {
        Map<ModWithItemCategorySortingRule, Vector<TileEntity>> modCatMap = new HashMap<>();
        for (TileEntity chest:chests) {
            StorageConfig config = chest.getCapability(STORAGE_CONFIG_CAPABILITY, null);
            Set<? extends AbstractSortingRule> rules = config.getRules(ModWithItemCategorySortingRule.class);
            if (rules != null && rules.size() > 0) {
                for (AbstractSortingRule rule : rules) {
                    modCatMap.computeIfAbsent((ModWithItemCategorySortingRule) rule, (k) -> new Vector<>());
                    modCatMap.get(rule).add(chest);
                }
            }
        }
        return modCatMap;
    }

    private static Map<ItemCategorySortingRule, Vector<TileEntity>> getItemCategoryMap(Vector<TileEntity> chests) {
        Map<ItemCategorySortingRule, Vector<TileEntity>> categoryMap = new HashMap<>();
        for (TileEntity chest:chests) {
            StorageConfig config = chest.getCapability(STORAGE_CONFIG_CAPABILITY, null);
            Set<? extends AbstractSortingRule> rules = config.getRules(ItemCategorySortingRule.class);
            if (rules != null && rules.size() > 0) {
                for (AbstractSortingRule rule : rules) {
                    categoryMap.computeIfAbsent((ItemCategorySortingRule) rule, (k) -> new Vector<>());
                    categoryMap.get(rule).add(chest);
                }
            }
        }
        return categoryMap;
    }

    private static Map<ModSortingRule, Vector<TileEntity>> getModMap(Vector<TileEntity> chests) {
        Map<ModSortingRule, Vector<TileEntity>> modMap = new HashMap<>();
        for (TileEntity chest:chests) {
            StorageConfig config = chest.getCapability(STORAGE_CONFIG_CAPABILITY, null);
            Set<? extends AbstractSortingRule> rules = config.getRules(ModSortingRule.class);
            if (rules != null && rules.size() > 0) {
                for (AbstractSortingRule rule : rules) {
                    modMap.computeIfAbsent((ModSortingRule) rule, (k) -> new Vector<>());
                    modMap.get(rule).add(chest);
                }
            }
        }
        return modMap;
    }

    // itemId match
    private static Vector<TileEntity> itemMatchPriOne(ItemStack istack, Map<ItemSortingRule, Vector<TileEntity>> itemMap) {
        ItemSortingRule rule = (ItemSortingRule) proxy.sortingRuleProvider.fromItemStack(istack, ItemSortingRule.class);
        rule.setUseNbt(true); // default to using nbt. rules in hashset will determine whether nbt is used.
        if (itemMap.containsKey(rule)) {
            return itemMap.get(rule);
        }
        return new Vector<>();
    }

    // mod with item category match
    private static Vector<TileEntity> itemMatchPriTwo(ItemStack istack, Map<ModWithItemCategorySortingRule, Vector<TileEntity>> modCatMap) {
        ModWithItemCategorySortingRule rule = (ModWithItemCategorySortingRule) proxy.sortingRuleProvider.fromItemStack(istack, ModWithItemCategorySortingRule.class);
        if (modCatMap.containsKey(rule)) {
            return modCatMap.get(rule);
        }
        return new Vector<>();
    }

    // item category match
    private static Vector<TileEntity> itemMatchPriThree(ItemStack istack, Map<ItemCategorySortingRule, Vector<TileEntity>> catMap) {
        ItemCategorySortingRule rule = (ItemCategorySortingRule) proxy.sortingRuleProvider.fromItemStack(istack, ItemCategorySortingRule.class);
        if (catMap.containsKey(rule)) {
            return catMap.get(rule);
        }
        return new Vector<>();
    }

    // mod match
    private static Vector<TileEntity> itemMatchPriFour(ItemStack istack, Map<ModSortingRule, Vector<TileEntity>> modMap) {
        ModSortingRule rule = (ModSortingRule) proxy.sortingRuleProvider.fromItemStack(istack, ModSortingRule.class);
        if (modMap.containsKey(rule)) {
            return modMap.get(rule);
        }
        return new Vector<>();
    }

    // allItems match
    private static Vector<TileEntity> itemMatchPriFive(Vector<TileEntity> chests) {
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
        IItemHandler itemHandler = getItemHandler(chest);
        ItemStack playerStack = player.inventory.getStackInSlot(playerInvIdx);
        if (itemHandler == null) {
            LOGGER.error("{} doesn't have an item handler, but it should", chest);
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

    private static void recordItemTransfer(ItemStack istack, int itemsStored, TileEntity chest, Map<BlockPos, List<ItemStack>> sortingResultsOut) {
        ItemStack newStack = istack.copy();
        newStack.setCount(itemsStored);
        sortingResultsOut.computeIfAbsent(chest.getPos(), (k) -> new Vector<>());
        sortingResultsOut.get(chest.getPos()).add(newStack);
    }

    @Override
    public IMessage onMessage(StoreItemsMessage message, MessageContext ctx) {
        EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
        serverPlayer.getServerWorld().addScheduledTask(() -> {
            final long startTime = System.nanoTime();
            Vector<TileEntity> nearbyChests = getLocalChests(serverPlayer);
            Map<BlockPos, List<ItemStack>> sortingResults = new HashMap<>();
            Map<String, Integer> sortStats = sortInventory(serverPlayer, nearbyChests, sortingResults);
            final long endTime = System.nanoTime();
            serverPlayer.sendMessage(
                    new TextComponentTranslation("exdepot.chatmessage.itemsStored",
                            sortStats.get("ItemsStored"),
                            sortStats.get("ChestsStoredTo"),
                            sortStats.get("ChestsStoredTo")==1?"":"s"
                    )
            );
            LOGGER.info("Storing items took "+(endTime-startTime)/1000000.0+" milliseconds");
            ExDepotMod.NETWORK.sendTo(new StoreItemsResponse(sortingResults), serverPlayer);
        });
        // No response packet
        return null;
    }
}
