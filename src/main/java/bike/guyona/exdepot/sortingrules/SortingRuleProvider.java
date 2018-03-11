package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.ExDepotMod;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortingRuleProvider {
    private Map<Class<? extends AbstractSortingRule>, AbstractSortingRuleFactory> factoryCache;

    public SortingRuleProvider() {
        factoryCache = new HashMap<>();
        factoryCache.put(ModSortingRule.class, new ModSortingRuleFactory());
        factoryCache.put(ItemCategorySortingRule.class, new ItemCategorySortingRuleFactory());
        factoryCache.put(ModWithItemCategorySortingRule.class, new ModWithItemCategorySortingRuleFactory());
        factoryCache.put(ItemSortingRule.class, new ItemSortingRuleFactory());
    }

    public AbstractSortingRule fromItemStack(ItemStack stack, Class<? extends AbstractSortingRule> ruleType) {
        if (factoryCache.get(ruleType) != null) {
            return factoryCache.get(ruleType).fromItemStack(stack);
        } else {
            ExDepotMod.LOGGER.error("{} does not have a factory registered", ruleType);
            return null;
        }
    }

    public AbstractSortingRule fromBytes(ByteBuffer bbuf, Class<? extends AbstractSortingRule> ruleType) {
        if (factoryCache.get(ruleType) != null) {
            return factoryCache.get(ruleType).fromBytes(bbuf);
        } else {
            ExDepotMod.LOGGER.error("{} does not have a factory registered", ruleType);
            return null;
        }
    }

    public List<? extends AbstractSortingRule> getAllRules(Class<? extends AbstractSortingRule> ruleType) {
        if (factoryCache.get(ruleType) != null) {
            return factoryCache.get(ruleType).getAllRules();
        } else {
            ExDepotMod.LOGGER.error("{} does not have a factory registered", ruleType.toString());
            return null;
        }
    }

    public List<TileEntity> getMatchingChests(ItemStack item, Map<? extends AbstractSortingRule, List<TileEntity>> chestsMap, Class<? extends AbstractSortingRule> ruleType) {
        if (factoryCache.get(ruleType) != null) {
            return factoryCache.get(ruleType).getMatchingChests(item, chestsMap);
        } else {
            ExDepotMod.LOGGER.error("{} does not have a factory registered", ruleType);
            return null;
        }
    }
}
