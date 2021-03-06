package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.sortingrules.item.ItemSortingRule;
import bike.guyona.exdepot.sortingrules.item.ItemSortingRuleFactory;
import bike.guyona.exdepot.sortingrules.itemcategory.ItemCategorySortingRule;
import bike.guyona.exdepot.sortingrules.itemcategory.ItemCategorySortingRuleFactory;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRuleFactory;
import bike.guyona.exdepot.sortingrules.modwithitemcategory.ModWithItemCategorySortingRule;
import bike.guyona.exdepot.sortingrules.modwithitemcategory.ModWithItemCategorySortingRuleFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SortingRuleProvider {
    public List<Class<? extends AbstractSortingRule>> ruleClasses;
    public Map<Class<? extends AbstractSortingRule>, String> ruleHeaders;

    private Map<Class<? extends AbstractSortingRule>, AbstractSortingRuleFactory> factoryCache;
    private Map<Class<? extends AbstractSortingRule>, String> headerCache;

    public SortingRuleProvider() {
        ruleClasses = new ArrayList<>();
        ruleHeaders = new HashMap<>();
        headerCache = new HashMap<>();
        factoryCache = new HashMap<>();

        ruleClasses.add(ItemSortingRule.class);
        ruleHeaders.put(ItemSortingRule.class, "exdepot.configgui.header.item");
        factoryCache.put(ItemSortingRule.class, new ItemSortingRuleFactory());
        ruleClasses.add(ModWithItemCategorySortingRule.class);
        ruleHeaders.put(ModWithItemCategorySortingRule.class, "exdepot.configgui.header.modwithitemcategory");
        factoryCache.put(ModWithItemCategorySortingRule.class, new ModWithItemCategorySortingRuleFactory());
        ruleClasses.add(ItemCategorySortingRule.class);
        ruleHeaders.put(ItemCategorySortingRule.class, "exdepot.configgui.header.itemcategory");
        factoryCache.put(ItemCategorySortingRule.class, new ItemCategorySortingRuleFactory());
        ruleClasses.add(ModSortingRule.class);
        ruleHeaders.put(ModSortingRule.class, "exdepot.configgui.header.mod");
        factoryCache.put(ModSortingRule.class, new ModSortingRuleFactory());
    }

    public Class<? extends AbstractSortingRule> getClassFromId(long serialVersionUID) {
        for (Class<? extends AbstractSortingRule> ruleClass : ruleClasses) {
            if (getIdFromClass(ruleClass) == serialVersionUID) {
                return ruleClass;
            }
        }
        ExDepotMod.LOGGER.error("We don't have a class registered with serialVersionUID={}.", serialVersionUID);
        return null;
    }

    public long getIdFromClass(Class<? extends AbstractSortingRule> ruleClass) {
        Field field = null;
        try {
            field = ruleClass.getDeclaredField("serialVersionUID");
            field.setAccessible(true);
            return field.getLong(ruleClass);
        } catch (NoSuchFieldException e) {
            ExDepotMod.LOGGER.error("Class {} does not have STATIC FINAL field serialVersionUID.", ruleClass);
        } catch (IllegalAccessException e) {
            ExDepotMod.LOGGER.error("Class {} has serialVersionUID, but it isn't accessible, or isn't a LONG.", ruleClass);
        }
        return -1;
    }

    public String getRuleTypeDisplayName(Class<? extends AbstractSortingRule> ruleClass) {
        if (headerCache.get(ruleClass) == null) {
            String ruleHeaderTranslationString = ruleHeaders.get(ruleClass);
            if (ruleHeaderTranslationString == null) {
                headerCache.put(ruleClass, "<Unknown Header>:");
            } else {
                headerCache.put(ruleClass, new TextComponentTranslation(ruleHeaderTranslationString).getUnformattedText() + ":");
            }
        }
        return headerCache.get(ruleClass);
    }

    public AbstractSortingRule fromItemStack(ItemStack stack, Class<? extends AbstractSortingRule> ruleType) {
        if (factoryCache.get(ruleType) != null) {
            return factoryCache.get(ruleType).fromItemStack(stack);
        } else {
            ExDepotMod.LOGGER.error("{} does not have a factory registered", ruleType);
            return null;
        }
    }

    public AbstractSortingRule fromBytes(ByteBuffer bbuf, int version, Class<? extends AbstractSortingRule> ruleType) {
        if (factoryCache.get(ruleType) != null) {
            return factoryCache.get(ruleType).fromBytes(bbuf, version);
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
