package bike.guyona.exdepot.capability;

import bike.guyona.exdepot.sortingrules.ItemCategorySortingRule;
import bike.guyona.exdepot.sortingrules.ItemSortingRule;
import bike.guyona.exdepot.sortingrules.ModSortingRule;
import bike.guyona.exdepot.sortingrules.ModWithItemCategorySortingRule;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.LinkedHashSet;
import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;

/**
 * Created by longb on 9/10/2017.
 *
 * A StorageConfig object remembers what items an inventory will pull from the player inventory.
 * When a player presses the configured key, the following operations occur:
 * 1. All inventories within the configured range from player are selected.
 * 2. The first item in the player's inventory is selected.
 * 3. The selected item is checked against the first rule class below, for every selected inventory.
 *    inventories are iterated over first by min Y, then min X, then min Z.
 * 4. The first inventory to match an item will have the item sent to it.
 * 5. Once all inventories have been checked, repeat step 2-5 for the next rule class.
 *
 * Rule classes:
 * 1. itemId match
 * 2. modId+category match
 * 3. category match
 * 4. modid match
 * 5. all items
 *
 * GUI buttons:
 * From Chest: Checks all items in chest. For any that aren't covered by the existing rules, adds that itemId to config.
 * (chest with green + in corner)
 * Clear: Removes all configuration rules from chest.
 * (chest with red X in corner)
 * ItemId: adds entered itemId to config. If a string, interprets the string as an item name, and looks up itemId.
 * (tab with text)
 * ModId: adds entered itemId to config.
 * (tab with text)
 * All Items: configures chest to accept all items.
 * (asterisk)
 */
public class StorageConfig implements Serializable {
    private static final int VERSION = 5;
    public LinkedHashSet<ItemSortingRule> itemIds;
    public LinkedHashSet<ModWithItemCategorySortingRule> modIdAndCategoryPairs;
    public LinkedHashSet<ItemCategorySortingRule> itemCategories;
    public LinkedHashSet<ModSortingRule> modIds;
    public boolean allItems;

    public StorageConfig() {
        itemIds = new LinkedHashSet<>();
        modIdAndCategoryPairs = new LinkedHashSet<>();
        itemCategories = new LinkedHashSet<>();
        modIds = new LinkedHashSet<>();
        allItems = false;
    }

    public StorageConfig(LinkedHashSet<ItemSortingRule> itemIds,
                         LinkedHashSet<ModWithItemCategorySortingRule> modIdAndCategoryPairs,
                         LinkedHashSet<ItemCategorySortingRule> itemCategories,
                         LinkedHashSet<ModSortingRule> modIds,
                         boolean allItems) {
        this.itemIds = itemIds;
        this.modIdAndCategoryPairs = modIdAndCategoryPairs;
        this.itemCategories = itemCategories;
        this.modIds = modIds;
        this.allItems = allItems;
    }

    public void copyFrom(StorageConfig conf) {
        itemIds = conf.itemIds;
        modIdAndCategoryPairs = conf.modIdAndCategoryPairs;
        itemCategories = conf.itemCategories;
        modIds = conf.modIds;
        allItems = conf.allItems;
    }

    public static StorageConfig fromBytes(byte[] buf) {
        ByteBuffer bbuf = ByteBuffer.wrap(buf);
        int version = bbuf.getInt();
        switch (version) {
            case 3:
                return fromBytesV3(bbuf, version);
            case 4:
            case 5:
                return fromBytesV4(bbuf, version);
            default:
                LOGGER.warn("Found a StorageConfig of version {}. Overwriting.", version);
                return new StorageConfig();
        }
    }

    public byte[] toBytes() {
        int totalSize = 0;
        totalSize += Integer.SIZE/8;// VERSION
        totalSize += Byte.SIZE/8;//initialized
        totalSize += Byte.SIZE/8;//allItems
        totalSize += Integer.SIZE/8;//itemIds size
        Vector<byte[]> itemBufs = new Vector<>();
        for (ItemSortingRule itemRule:itemIds) {
            byte[] itemRuleBuf = itemRule.toBytes();
            totalSize += itemRuleBuf.length;
            itemBufs.add(itemRuleBuf);
        }

        totalSize += Integer.SIZE/8;//mod+cat size
        Vector<byte[]> modCatBufs = new Vector<>();
        for (ModWithItemCategorySortingRule modCatRule:modIdAndCategoryPairs) {
            byte[] modCatRuleBuf = modCatRule.toBytes();
            totalSize += modCatRuleBuf.length;
            modCatBufs.add(modCatRuleBuf);
        }
        totalSize += Integer.SIZE/8;//category size
        Vector<byte[]> categoryBufs = new Vector<>();
        for (ItemCategorySortingRule catRule:itemCategories) {
            byte[] catRuleBuf = catRule.toBytes();
            totalSize += catRuleBuf.length;
            categoryBufs.add(catRuleBuf);
        }
        totalSize += Integer.SIZE/8;//modIds size
        Vector<byte[]> modIdBufs = new Vector<>();
        for (ModSortingRule modRule:modIds) {
            byte[] modId = modRule.toBytes();
            modIdBufs.add(modId);
            totalSize += modId.length;//modId
        }

        ByteBuffer outBuf = ByteBuffer.allocate(totalSize);

        outBuf.putInt(VERSION);
        outBuf.put((byte)(allItems?1:0));
        outBuf.putInt(itemBufs.size());
        for (byte[] stack:itemBufs) {
            outBuf.put(stack);
        }
        outBuf.putInt(modCatBufs.size());
        for (byte[] modCat:modCatBufs) {
            outBuf.put(modCat);
        }
        outBuf.putInt(categoryBufs.size());
        for (byte[] catLabel:categoryBufs) {
            outBuf.put(catLabel);
        }
        outBuf.putInt(modIdBufs.size());
        for (byte[] modId:modIdBufs) {
            outBuf.put(modId);
        }
        return outBuf.array();
    }

    private static StorageConfig fromBytesV3(ByteBuffer bbuf, int version) {
        boolean allItems = bbuf.get() != 0;
        LinkedHashSet<ItemSortingRule> itemIds = new LinkedHashSet<>();
        int idCount = bbuf.getInt();
        for (int i=0; i<idCount; i++) {
            ItemSortingRule rule = (ItemSortingRule) proxy.sortingRuleProvider.fromBytes(bbuf, version, ItemSortingRule.class);
            if (rule != null)
                itemIds.add(rule);
        }
        LinkedHashSet<ModSortingRule> modIds = new LinkedHashSet<>();
        int modCount = bbuf.getInt();
        for (int i=0; i<modCount; i++) {
            ModSortingRule rule = (ModSortingRule) proxy.sortingRuleProvider.fromBytes(bbuf, version, ModSortingRule.class);
            if (rule != null)
                modIds.add(rule);
        }
        return new StorageConfig(itemIds, new LinkedHashSet<>(), new LinkedHashSet<>(), modIds, allItems);
    }

    private static StorageConfig fromBytesV4(ByteBuffer bbuf, int version) {
        boolean allItems = bbuf.get() != 0;
        LinkedHashSet<ItemSortingRule> itemIds = new LinkedHashSet<>();
        int idCount = bbuf.getInt();
        for (int i=0; i<idCount; i++) {
            ItemSortingRule rule = (ItemSortingRule) proxy.sortingRuleProvider.fromBytes(bbuf, version, ItemSortingRule.class);
            if (rule != null)
                itemIds.add(rule);
        }
        LinkedHashSet<ModWithItemCategorySortingRule> modCats = new LinkedHashSet<>();
        int modCatCount = bbuf.getInt();
        for (int i=0; i<modCatCount; i++) {
            ModWithItemCategorySortingRule rule = (ModWithItemCategorySortingRule) proxy.sortingRuleProvider.fromBytes(bbuf, version, ModWithItemCategorySortingRule.class);
            if (rule != null)
                modCats.add(rule);
        }
        LinkedHashSet<ItemCategorySortingRule> cats = new LinkedHashSet<>();
        int catCount = bbuf.getInt();
        for (int i=0; i<catCount; i++) {
            ItemCategorySortingRule rule = (ItemCategorySortingRule) proxy.sortingRuleProvider.fromBytes(bbuf, version, ItemCategorySortingRule.class);
            if (rule != null)
                cats.add(rule);
        }
        LinkedHashSet<ModSortingRule> modIds = new LinkedHashSet<>();
        int modCount = bbuf.getInt();
        for (int i=0; i<modCount; i++) {
            ModSortingRule rule = (ModSortingRule) proxy.sortingRuleProvider.fromBytes(bbuf, version, ModSortingRule.class);
            if (rule != null)
                modIds.add(rule);
        }
        return new StorageConfig(itemIds, modCats, cats, modIds, allItems);
    }
}
