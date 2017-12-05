package bike.guyona.exdepot.storageconfig.capability;

import bike.guyona.exdepot.ExDepotMod;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.SlicedByteBuf;
import io.netty.buffer.UnpooledHeapByteBuf;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

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
    private static final int VERSION = 0;
    public boolean initialized;
    public boolean allItems;
    //TODO: These may be interesting, albeit expensive. I'd need a list of the health to match for each item.
    //TODO: And NBT matching would have to be on the fly. F that.
    //public boolean matchHealth;
    //public boolean matchNBT;
    public Vector<Integer> itemIds;
    public Vector<String> modIds;

    public StorageConfig() {
        initialized = false;
        allItems = false;
        itemIds = new Vector<>();
        modIds = new Vector<>();
    }

    public StorageConfig(boolean allItems, Vector<Integer> itemIds, Vector<String> modIds) {
        this.initialized = true;
        this.allItems = allItems;
        this.itemIds = itemIds;
        this.modIds = modIds;
    }

    public StorageConfig(boolean initialized, boolean allItems, Vector<Integer> itemIds, Vector<String> modIds) {
        this.initialized = initialized;
        this.allItems = allItems;
        this.itemIds = itemIds;
        this.modIds = modIds;
    }

    public void copyFrom(StorageConfig conf) {
        initialized = conf.initialized;
        allItems = conf.allItems;
        itemIds = conf.itemIds;
        modIds = conf.modIds;
    }

    public static StorageConfig fromBytes2(byte[] bytes){
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return (StorageConfig) in.readObject();
        } catch (IOException ex) {
            ExDepotMod.LOGGER.error("Couldn't write byte array for storage object: %s", ex.getMessage());
            return new StorageConfig();
        } catch (ClassNotFoundException ex) {
            ExDepotMod.LOGGER.error("Couldn't find class for storage object: %s", ex.getMessage());
            return new StorageConfig();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    public byte[] toBytes2(){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            return bos.toByteArray();
        } catch (IOException ex) {
            ExDepotMod.LOGGER.error("Couldn't write byte array for storage object: %s", ex.getMessage());
            return new byte[0];
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                ExDepotMod.LOGGER.error("Couldn't close stream for storage object: %s", ex.getMessage());
                return new byte[0];
            }
        }
    }

    public static StorageConfig fromBytes(byte[] buf) {
        ByteBuffer bbuf = ByteBuffer.wrap(buf);
        if (bbuf.getInt() != VERSION) {
            return null;
        }
        boolean initialized = bbuf.get() != 0;
        boolean allItems = bbuf.get() != 0;
        Vector<Integer> itemIds = new Vector<>();
        int idCount = bbuf.getInt();
        for (int i=0; i<idCount; i++) {
            itemIds.add(bbuf.getInt());
        }
        Vector<String> modIds = new Vector<>();
        int modCount = bbuf.get();
        for (int i=0; i<modCount; i++) {
            int modIdLen = bbuf.get();
            byte[] modIdBuf = new byte[modIdLen];
            bbuf.get(modIdBuf, bbuf.arrayOffset(), modIdLen);
            String modId = new String(modIdBuf, StandardCharsets.UTF_8);
            modIds.add(modId);
        }
        return new StorageConfig(initialized, allItems, itemIds, modIds);
    }

    public byte[] toBytes() {
        int totalSize = 0;
        totalSize += Integer.SIZE/8;// VERSION
        totalSize += Byte.SIZE/8;//initialized
        totalSize += Byte.SIZE/8;//allItems
        totalSize += Integer.SIZE/8;//itemIds size
        totalSize += Integer.SIZE/8 * itemIds.size();//itemIds
        totalSize += Integer.SIZE/8;//modIds size
        Vector<byte[]> modIdBufs = new Vector<>();
        for (int i=0; i<modIds.size(); i++) {
            byte[] modId = modIds.get(i).getBytes(StandardCharsets.UTF_8);
            modIdBufs.add(modId);
            totalSize += Integer.SIZE/8;//modId size
            totalSize += modId.length;//modId
        }

        ByteBuffer outBuf = ByteBuffer.allocate(totalSize);

        outBuf.putInt(VERSION);
        outBuf.put((byte)(initialized?1:0));
        outBuf.put((byte)(allItems?1:0));
        outBuf.putInt(itemIds.size());
        for (Integer itemId : itemIds) {
            outBuf.putInt(itemId);
        }
        outBuf.putInt(modIds.size());
        for (byte[] modId : modIdBufs) {
            outBuf.putInt(modId.length);
            outBuf.put(modId);
        }
        return outBuf.array();
    }
}
