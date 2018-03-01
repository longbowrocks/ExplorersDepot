package bike.guyona.exdepot.capability;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

/**
 * Created by longb on 9/10/2017.
 *
 * A StorageConfig object remembers what items an inventory will pull from the player inventory.
 * When a player presses the configured key, the following operations occur:
 * 1. All inventories within the configured range from player are selected.
 * 2. The first item in the player's inventory is selected.
 * 3. The selected item is checked against the first rule class below, for every selected inventory.
 *    inventories are iterated over first by min X, then min Y, then min Z.
 * 4. The first inventory to match an item will have the item sent to it.
 * 5. Once all inventories have been checked, repeat step 2-5 for the next rule class.
 *
 * Rule classes:
 * 1. itemId match
 * 2. modid match
 * 3. all items
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
    private static final int VERSION = 2;
    public boolean allItems;
    public Set<Integer> itemIds;
    public Set<String> modIds;

    public StorageConfig() {
        allItems = false;
        itemIds = new LinkedHashSet<>();
        modIds = new LinkedHashSet<>();
    }

    public StorageConfig(boolean allItems, LinkedHashSet<Integer> itemIds, LinkedHashSet<String> modIds) {
        this.allItems = allItems;
        this.itemIds = itemIds;
        this.modIds = modIds;
    }

    public void copyFrom(StorageConfig conf) {
        allItems = conf.allItems;
        itemIds = conf.itemIds;
        modIds = conf.modIds;
    }

    public static StorageConfig fromBytes(byte[] buf) {
        ByteBuffer bbuf = ByteBuffer.wrap(buf);
        if (bbuf.getInt() != VERSION) {
            LOGGER.warn("Found a StorageConfig of an old version. Overwriting.");
            return new StorageConfig();
        }
        boolean allItems = bbuf.get() != 0;
        LinkedHashSet<Integer> itemIds = new LinkedHashSet<>();
        int idCount = bbuf.getInt();
        for (int i=0; i<idCount; i++) {
            int itemIdLen = bbuf.getInt();
            byte[] itemIdBuf = new byte[itemIdLen];
            bbuf.get(itemIdBuf, bbuf.arrayOffset(), itemIdLen);
            String itemId = new String(itemIdBuf, StandardCharsets.UTF_8);
            int itemIntId = Item.getIdFromItem(Item.getByNameOrId(itemId));
            itemIds.add(itemIntId);
        }
        LinkedHashSet<String> modIds = new LinkedHashSet<>();
        int modCount = bbuf.getInt();
        for (int i=0; i<modCount; i++) {
            int modIdLen = bbuf.getInt();
            byte[] modIdBuf = new byte[modIdLen];
            bbuf.get(modIdBuf, bbuf.arrayOffset(), modIdLen);
            String modId = new String(modIdBuf, StandardCharsets.UTF_8);
            modIds.add(modId);
        }
        return new StorageConfig(allItems, itemIds, modIds);
    }

    public byte[] toBytes() {
        int totalSize = 0;
        totalSize += Integer.SIZE/8;// VERSION
        totalSize += Byte.SIZE/8;//initialized
        totalSize += Byte.SIZE/8;//allItems
        totalSize += Integer.SIZE/8;//itemIds size
        Vector<byte[]> itemIdBufs = new Vector<>();
        for (Integer itemIdInt:itemIds) {
            byte[] itemId = Item.getItemById(itemIdInt).getRegistryName().toString().getBytes(StandardCharsets.UTF_8);
            itemIdBufs.add(itemId);
            totalSize += Integer.SIZE/8;//itemId size
            totalSize += itemId.length;//itemId
        }
        totalSize += Integer.SIZE/8;//modIds size
        Vector<byte[]> modIdBufs = new Vector<>();
        for (String modIdString:modIds) {
            byte[] modId = modIdString.getBytes(StandardCharsets.UTF_8);
            modIdBufs.add(modId);
            totalSize += Integer.SIZE/8;//modId size
            totalSize += modId.length;//modId
        }

        ByteBuffer outBuf = ByteBuffer.allocate(totalSize);

        outBuf.putInt(VERSION);
        outBuf.put((byte)(allItems?1:0));
        outBuf.putInt(itemIds.size());
        for (byte[] itemId : itemIdBufs) {
            outBuf.putInt(itemId.length);
            outBuf.put(itemId);
        }
        outBuf.putInt(modIds.size());
        for (byte[] modId : modIdBufs) {
            outBuf.putInt(modId.length);
            outBuf.put(modId);
        }
        return outBuf.array();
    }
}
