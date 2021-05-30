package bike.guyona.exdepot.capability;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.sortingrules.*;
import bike.guyona.exdepot.sortingrules.item.ItemSortingRule;
import bike.guyona.exdepot.sortingrules.itemcategory.ItemCategorySortingRule;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import bike.guyona.exdepot.sortingrules.modwithitemcategory.ModWithItemCategorySortingRule;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.LazyOptional;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

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
    private static final int VERSION = 8;
    private Map<Class<? extends AbstractSortingRule>, LinkedHashSet<AbstractSortingRule>> rules;
    public boolean allItems;
    private boolean useNbt;

    public StorageConfig() {
        rules = new HashMap<>();
        allItems = false;
        useNbt = true;
    }

    public static StorageConfig fromTileEntity(TileEntity tileEntity) {
        LazyOptional<StorageConfig> conf = tileEntity.getCapability(StorageConfigProvider.STORAGE_CONFIG_CAPABILITY, null);
        if (conf.isPresent()) {
            StorageConfig realConf = conf.orElse(null);
            if (!realConf.isEmpty()) {
                //TODO: yeah I'll goddamn bet it's @NotNull. Stop trying to make method chaining happen on capabilities.
                return conf.orElse(null);
            }
        } else {
            LOGGER.error("StorageConfig was never added to {} for some reason.", tileEntity);
        }
        return null;
    }

    public boolean isEmpty() {
        for (LinkedHashSet<AbstractSortingRule> ruleSet : rules.values()) {
            if (ruleSet.size() > 0) {
                return false;
            }
        }
        if (allItems){
            return false;
        }
        return true;
    }

    public void addRule(AbstractSortingRule rule) {
        rules.computeIfAbsent(rule.getClass(), k -> new LinkedHashSet<>());
        if (rule instanceof ItemSortingRule) {
            ((ItemSortingRule) rule).setUseNbt(useNbt);
        }
        rules.get(rule.getClass()).add(rule);
    }

    public Set<? extends AbstractSortingRule> getRules(Class<? extends AbstractSortingRule> ruleClass) {
        return rules.get(ruleClass);
    }

    public void setUseNbt(boolean useNbt) {
        this.useNbt = useNbt;
        if (rules.get(ItemSortingRule.class) == null) {
            return;
        }
        for (AbstractSortingRule rule : rules.get(ItemSortingRule.class)) {
            ((ItemSortingRule)rule).setUseNbt(useNbt);
        }
    }

    public boolean getUseNbt() {
        return useNbt;
    }

    public void copyFrom(StorageConfig conf) {
        rules = new HashMap<>();
        for (Class<? extends AbstractSortingRule> ruleClass : conf.rules.keySet()) {
            rules.computeIfAbsent(ruleClass, k -> new LinkedHashSet<>());
            rules.put(ruleClass, conf.rules.get(ruleClass));
        }
        allItems = conf.allItems;
        setUseNbt(conf.useNbt);
    }

    public static StorageConfig fromBytes(byte[] buf) {
        ByteBuffer bbuf = ByteBuffer.wrap(buf);
        int version = bbuf.getInt();
        if (version <= 3) {
            return fromBytesV3(bbuf, version);
        } else if (version <= 5) {
            return fromBytesV4(bbuf, version);
        } else if (version <= 6) {
            return fromBytesV6(bbuf, version);
        }
        return fromBytesV7(bbuf, version);
    }

    public byte[] toBytes() {
        int totalSize = 0;
        totalSize += Integer.SIZE/8;// VERSION
        totalSize += Byte.SIZE/8;//allItems
        totalSize += Byte.SIZE/8;//useNbt

        totalSize += Integer.SIZE/8;//numRuleTypes
        Map<Class<? extends AbstractSortingRule>, LinkedHashSet<byte[]>> ruleBufs = new HashMap<>();
        for (Class<? extends AbstractSortingRule> ruleClass : rules.keySet()) {
            totalSize += Long.SIZE/8;//ruleType
            totalSize += Integer.SIZE/8;//numRules
            ruleBufs.computeIfAbsent(ruleClass, k -> new LinkedHashSet<>());
            for (AbstractSortingRule rule : rules.get(ruleClass)) {
                byte[] ruleBuf = rule.toBytes();
                totalSize += ruleBuf.length;
                ruleBufs.get(ruleClass).add(ruleBuf);
            }
        }

        ByteBuffer outBuf = ByteBuffer.allocate(totalSize);

        outBuf.putInt(VERSION);
        outBuf.put((byte)(allItems?1:0));
        outBuf.put((byte)(useNbt ?1:0));

        outBuf.putInt(rules.size());
        for (Class<? extends AbstractSortingRule> ruleClass : ruleBufs.keySet()) {
            long ruleTypeId = ExDepotMod.sortingRuleProvider.getIdFromClass(ruleClass);
            if (ruleTypeId == -1) {
                LOGGER.error("Unknown rule class {}", ruleClass);
                return new byte[] {0,0,0,0};
            }
            outBuf.putLong(ruleTypeId);
            outBuf.putInt(ruleBufs.get(ruleClass).size());
            for (byte[] ruleBuf : ruleBufs.get(ruleClass)) {
                outBuf.put(ruleBuf);
            }
        }
        return outBuf.array();
    }

    private static StorageConfig fromBytesV3(ByteBuffer bbuf, int version) {
        StorageConfig storageConfig = new StorageConfig();
        boolean allItems = bbuf.get() != 0;
        storageConfig.allItems = allItems;

        List<Class<? extends AbstractSortingRule>> ruleClassesToRead = Arrays.asList(ItemSortingRule.class, ModSortingRule.class);
        for (Class<? extends AbstractSortingRule> ruleClass : ruleClassesToRead) {
            int ruleCount = bbuf.getInt();
            for (int i=0; i<ruleCount; i++) {
                AbstractSortingRule rule = ExDepotMod.sortingRuleProvider.fromBytes(bbuf, version, ruleClass);
                if (rule == null) {
                    LOGGER.error("Version {} is not supported for rule type {}. overwriting StorageConfig", version, ruleClass);
                    return new StorageConfig();
                }
                storageConfig.addRule(rule);
            }
        }
        storageConfig.setUseNbt(true);
        return storageConfig;
    }

    private static StorageConfig fromBytesV4(ByteBuffer bbuf, int version) {
        StorageConfig storageConfig = new StorageConfig();
        boolean allItems = bbuf.get() != 0;
        storageConfig.allItems = allItems;

        List<Class<? extends AbstractSortingRule>> ruleClassesToRead = Arrays.asList(
                ItemSortingRule.class,
                ModWithItemCategorySortingRule.class,
                ItemCategorySortingRule.class,
                ModSortingRule.class);
        for (Class<? extends AbstractSortingRule> ruleClass : ruleClassesToRead) {
            int ruleCount = bbuf.getInt();
            for (int i=0; i<ruleCount; i++) {
                AbstractSortingRule rule = ExDepotMod.sortingRuleProvider.fromBytes(bbuf, version, ruleClass);
                if (rule == null) {
                    LOGGER.error("Version {} is not supported for rule type {}. overwriting StorageConfig", version, ruleClass);
                    return new StorageConfig();
                }
                storageConfig.addRule(rule);
            }
        }
        storageConfig.setUseNbt(true);
        return storageConfig;
    }

    private static StorageConfig fromBytesV6(ByteBuffer bbuf, int version) {
        StorageConfig storageConfig = new StorageConfig();
        boolean allItems = bbuf.get() != 0;
        storageConfig.allItems = allItems;
        boolean useNbt = bbuf.get() != 0;

        List<Class<? extends AbstractSortingRule>> ruleClassesToRead = Arrays.asList(
                ItemSortingRule.class,
                ModWithItemCategorySortingRule.class,
                ItemCategorySortingRule.class,
                ModSortingRule.class);
        for (Class<? extends AbstractSortingRule> ruleClass : ruleClassesToRead) {
            int ruleCount = bbuf.getInt();
            for (int i=0; i<ruleCount; i++) {
                AbstractSortingRule rule = ExDepotMod.sortingRuleProvider.fromBytes(bbuf, version, ruleClass);
                if (rule == null) {
                    LOGGER.error("Version {} is not supported for rule type {}. overwriting StorageConfig", version, ruleClass);
                    return new StorageConfig();
                }
                storageConfig.addRule(rule);
            }
        }
        storageConfig.setUseNbt(useNbt);
        return storageConfig;
    }

    private static StorageConfig fromBytesV7(ByteBuffer bbuf, int version) {
        StorageConfig storageConfig = new StorageConfig();
        boolean allItems = bbuf.get() != 0;
        storageConfig.allItems = allItems;
        boolean useNbt = bbuf.get() != 0;

        int numRuleClasses = bbuf.getInt();
        for (int i=0; i< numRuleClasses; i++) {
            long ruleTypeId = bbuf.getLong();
            Class<? extends AbstractSortingRule> ruleClass = ExDepotMod.sortingRuleProvider.getClassFromId(ruleTypeId);
            if (ruleClass == null) {
                LOGGER.error("Unknown rule serialVersionUID {}. returning empty StorageConfig.", ruleTypeId);
                return new StorageConfig();
            }
            int ruleCount = bbuf.getInt();
            for (int j=0; j<ruleCount; j++) {
                AbstractSortingRule rule = ExDepotMod.sortingRuleProvider.fromBytes(bbuf, version, ruleClass);
                if (rule == null) {
                    LOGGER.error("Version {} is not supported for rule type {}. overwriting StorageConfig", version, ruleClass);
                    return new StorageConfig();
                }
                storageConfig.addRule(rule);
            }
        }
        storageConfig.setUseNbt(useNbt);
        return storageConfig;
    }
}
