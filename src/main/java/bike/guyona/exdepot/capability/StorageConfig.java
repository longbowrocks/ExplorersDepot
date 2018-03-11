package bike.guyona.exdepot.capability;

import bike.guyona.exdepot.helpers.TrackableModCategoryPair;
import bike.guyona.exdepot.helpers.TrackableItemStack;
import bike.guyona.exdepot.sortingrules.ModSortingRule;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.instance;
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
    private static final int VERSION = 4;
    public LinkedHashSet<TrackableItemStack> itemIds;
    public LinkedHashSet<TrackableModCategoryPair> modIdAndCategoryPairs;
    public LinkedHashSet<String> itemCategories;
    public LinkedHashSet<ModSortingRule> modIds;
    public boolean allItems;

    public StorageConfig() {
        itemIds = new LinkedHashSet<>();
        modIdAndCategoryPairs = new LinkedHashSet<>();
        itemCategories = new LinkedHashSet<>();
        modIds = new LinkedHashSet<>();
        allItems = false;
    }

    public StorageConfig(LinkedHashSet<TrackableItemStack> itemIds,
                         LinkedHashSet<TrackableModCategoryPair> modIdAndCategoryPairs,
                         LinkedHashSet<String> itemCategories,
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
            case 2:
                return fromBytesV2(bbuf);
            case 3:
                return fromBytesV3(bbuf);
            case 4:
                return fromBytesV4(bbuf);
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
        for (TrackableItemStack stack:itemIds) {
            byte[] itemId = stack.itemId.getBytes(StandardCharsets.UTF_8);
            totalSize += Integer.SIZE/8;//itemId size
            totalSize += itemId.length;//itemId
            totalSize += Integer.SIZE/8;//itemSubtypeId size
        }

        totalSize += Integer.SIZE/8;//mod+cat size
        for (TrackableModCategoryPair modCat:modIdAndCategoryPairs) {
            byte[] modId = modCat.modId.getBytes(StandardCharsets.UTF_8);
            totalSize += Integer.SIZE/8;//mod size
            totalSize += modId.length;//mod
            byte[] catLabel = modCat.itemCategory.getBytes(StandardCharsets.UTF_8);
            totalSize += Integer.SIZE/8;//mod size
            totalSize += catLabel.length;//mod
        }
        totalSize += Integer.SIZE/8;//category size
        Vector<byte[]> categoryBufs = new Vector<>();
        for (String catString:itemCategories) {
            byte[] catLabel = catString.getBytes(StandardCharsets.UTF_8);
            categoryBufs.add(catLabel);
            totalSize += Integer.SIZE/8;//category size
            totalSize += catLabel.length;//category
        }
        totalSize += Integer.SIZE/8;//modIds size
        Vector<byte[]> modIdBufs = new Vector<>();
        for (ModSortingRule modRule:modIds) {
            byte[] modId = modRule.toBytes();
            modIdBufs.add(modId);
            totalSize += Integer.SIZE/8;//modId size
            totalSize += modId.length;//modId
        }

        ByteBuffer outBuf = ByteBuffer.allocate(totalSize);

        outBuf.putInt(VERSION);
        outBuf.put((byte)(allItems?1:0));
        outBuf.putInt(itemIds.size());
        for (TrackableItemStack stack:itemIds) {
            byte[] itemId = stack.itemId.getBytes(StandardCharsets.UTF_8);
            outBuf.putInt(itemId.length);
            outBuf.put(itemId);
            outBuf.putInt(stack.itemDamage);
        }
        outBuf.putInt(modIdAndCategoryPairs.size());
        for (TrackableModCategoryPair modCat:modIdAndCategoryPairs) {
            byte[] modId = modCat.modId.getBytes(StandardCharsets.UTF_8);
            outBuf.putInt(modId.length);
            outBuf.put(modId);
            byte[] catLabel = modCat.itemCategory.getBytes(StandardCharsets.UTF_8);
            outBuf.putInt(catLabel.length);
            outBuf.put(catLabel);
        }
        outBuf.putInt(categoryBufs.size());
        for (byte[] catLabel:categoryBufs) {
            outBuf.putInt(catLabel.length);
            outBuf.put(catLabel);
        }
        outBuf.putInt(modIdBufs.size());
        for (byte[] modId : modIdBufs) {
            outBuf.putInt(modId.length);
            outBuf.put(modId);
        }
        return outBuf.array();
    }

    private static StorageConfig fromBytesV2(ByteBuffer bbuf) {
        boolean allItems = bbuf.get() != 0;
        LinkedHashSet<TrackableItemStack> itemIds = new LinkedHashSet<>();
        int idCount = bbuf.getInt();
        for (int i=0; i<idCount; i++) {
            int itemIdLen = bbuf.getInt();
            byte[] itemIdBuf = new byte[itemIdLen];
            bbuf.get(itemIdBuf, bbuf.arrayOffset(), itemIdLen);
            String itemId = new String(itemIdBuf, StandardCharsets.UTF_8);
            itemIds.add(new TrackableItemStack(itemId, 0));
        }
        LinkedHashSet<ModSortingRule> modIds = new LinkedHashSet<>();
        int modCount = bbuf.getInt();
        for (int i=0; i<modCount; i++) {
            ModSortingRule rule = (ModSortingRule) proxy.sortingRuleProvider.fromBytes(bbuf, ModSortingRule.class);
            modIds.add(rule);
        }
        return new StorageConfig(itemIds, new LinkedHashSet<>(), new LinkedHashSet<>(), modIds, allItems);
    }

    private static StorageConfig fromBytesV3(ByteBuffer bbuf) {
        boolean allItems = bbuf.get() != 0;
        LinkedHashSet<TrackableItemStack> itemIds = new LinkedHashSet<>();
        int idCount = bbuf.getInt();
        for (int i=0; i<idCount; i++) {
            int itemIdLen = bbuf.getInt();
            byte[] itemIdBuf = new byte[itemIdLen];
            bbuf.get(itemIdBuf, bbuf.arrayOffset(), itemIdLen);
            String itemId = new String(itemIdBuf, StandardCharsets.UTF_8);
            int itemSubtypeId = bbuf.getInt();
            itemIds.add(new TrackableItemStack(itemId, itemSubtypeId));
        }
        LinkedHashSet<ModSortingRule> modIds = new LinkedHashSet<>();
        int modCount = bbuf.getInt();
        for (int i=0; i<modCount; i++) {
            ModSortingRule rule = (ModSortingRule) proxy.sortingRuleProvider.fromBytes(bbuf, ModSortingRule.class);
            modIds.add(rule);
        }
        return new StorageConfig(itemIds, new LinkedHashSet<>(), new LinkedHashSet<>(), modIds, allItems);
    }

    private static StorageConfig fromBytesV4(ByteBuffer bbuf) {
        boolean allItems = bbuf.get() != 0;
        LinkedHashSet<TrackableItemStack> itemIds = new LinkedHashSet<>();
        int idCount = bbuf.getInt();
        for (int i=0; i<idCount; i++) {
            int itemIdLen = bbuf.getInt();
            byte[] itemIdBuf = new byte[itemIdLen];
            bbuf.get(itemIdBuf, bbuf.arrayOffset(), itemIdLen);
            String itemId = new String(itemIdBuf, StandardCharsets.UTF_8);
            int itemSubtypeId = bbuf.getInt();
            itemIds.add(new TrackableItemStack(itemId, itemSubtypeId));
        }
        LinkedHashSet<TrackableModCategoryPair> modCats = new LinkedHashSet<>();
        int modCatCount = bbuf.getInt();
        for (int i=0; i<modCatCount; i++) {
            int modIdLen = bbuf.getInt();
            byte[] modIdBuf = new byte[modIdLen];
            bbuf.get(modIdBuf, bbuf.arrayOffset(), modIdLen);
            String modId = new String(modIdBuf, StandardCharsets.UTF_8);
            int catLen = bbuf.getInt();
            byte[] catBuf = new byte[catLen];
            bbuf.get(catBuf, bbuf.arrayOffset(), catLen);
            String catLabel = new String(catBuf, StandardCharsets.UTF_8);
            modCats.add(new TrackableModCategoryPair(modId, catLabel));
        }
        LinkedHashSet<String> cats = new LinkedHashSet<>();
        int catCount = bbuf.getInt();
        for (int i=0; i<catCount; i++) {
            int catLen = bbuf.getInt();
            byte[] catBuf = new byte[catLen];
            bbuf.get(catBuf, bbuf.arrayOffset(), catLen);
            String catLabel = new String(catBuf, StandardCharsets.UTF_8);
            cats.add(catLabel);
        }
        LinkedHashSet<ModSortingRule> modIds = new LinkedHashSet<>();
        int modCount = bbuf.getInt();
        for (int i=0; i<modCount; i++) {
            ModSortingRule rule = (ModSortingRule) proxy.sortingRuleProvider.fromBytes(bbuf, ModSortingRule.class);
            modIds.add(rule);
        }
        return new StorageConfig(itemIds, modCats, cats, modIds, allItems);
    }
}
