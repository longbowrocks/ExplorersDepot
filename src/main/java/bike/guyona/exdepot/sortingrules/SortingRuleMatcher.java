package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.capability.StorageConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.proxy;
import static bike.guyona.exdepot.capability.StorageConfigProvider.STORAGE_CONFIG_CAPABILITY;

public class SortingRuleMatcher {
//    private Vector<TileEntity> chests;
//
//    public SortingRuleMatcher(Vector<TileEntity> chests) {
//        this.chests = chests;
//    }
//
//    private boolean match(ItemStack stack) {
//        for (TileEntity chest : chests){
//            if (matchChest(stack, chest)){
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private static boolean matchChest(ItemStack stack, TileEntity chest) {
//        StorageConfig config = chest.getCapability(STORAGE_CONFIG_CAPABILITY, null);
//        if (config == null) {
//            return false;
//        }
//        return matchConfig(stack, config);
//    }

    public static boolean matchConfig(ItemStack stack, StorageConfig config) {
        for (Class<? extends AbstractSortingRule> ruleClass : proxy.sortingRuleProvider.ruleClasses) {
            AbstractSortingRule itemRule =  proxy.sortingRuleProvider.fromItemStack(stack, ruleClass);
            Set<? extends AbstractSortingRule> sortingRules = config.getRules(ruleClass);
            if (sortingRules == null){
                continue;
            }
            for (AbstractSortingRule rule : sortingRules) {
                if (rule.equals(itemRule)) {
                    return true;
                }
            }
        }
        return false;
    }
}
