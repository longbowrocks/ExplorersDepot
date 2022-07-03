package bike.guyona.exdepot.capabilities;


import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.SortingRuleProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DefaultDepotCapability implements IDepotCapability {
    private static final int VERSION = 8;
    // TODO: use an enum or something for the key. If the classloader is swapped out mid-game this could be evil.
    private Map<Class<? extends AbstractSortingRule>, Set<AbstractSortingRule>> rulesByType;

    public DefaultDepotCapability() {
        rulesByType = new HashMap<>();
    }

    @Override
    public boolean isEmpty() {
        for (Set<AbstractSortingRule> ruleSet : rulesByType.values()) {
            if (ruleSet.size() > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public <T extends AbstractSortingRule> void addRule(T rule) {
        rulesByType.computeIfAbsent(rule.getClass(), k -> new HashSet<>());
        rulesByType.get(rule.getClass()).add(rule);
    }


    private void replaceRules(Class<? extends AbstractSortingRule> ruleClass, Set<? extends AbstractSortingRule> rules) {
        rulesByType.put(ruleClass, (Set<AbstractSortingRule>) rules);
    }

    @Override
    @NotNull
    public <T extends AbstractSortingRule> Set<T> getRules(Class<T> ruleClass) {
        return (Set<T>) rulesByType.getOrDefault(ruleClass, new HashSet<>());
    }

    @Override
    public Set<Class<? extends AbstractSortingRule>> getRuleClasses() {
        return rulesByType.keySet();
    }

    @Override
    public void copyFrom(IDepotCapability cap) {
        rulesByType = new HashMap<>();
        for (Class<? extends AbstractSortingRule> ruleClass : cap.getRuleClasses()) {
            replaceRules(ruleClass, cap.getRules(ruleClass));
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        SortingRuleProvider matcherProvider = new SortingRuleProvider();
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("Version", VERSION);
        List<Long> typesList = new ArrayList<>();
        for (Class<? extends AbstractSortingRule> ruleClass : getRuleClasses()) {
            typesList.add(matcherProvider.getMatcherSerializableUID(ruleClass));
            ListTag matcherTagList = new ListTag();
            for (AbstractSortingRule matcher : getRules(ruleClass)) {
                CompoundTag matcherTag = new CompoundTag();
                matcher.save(matcherTag);
                matcherTagList.add(matcherTag);
            }
            nbt.put("TagList"+matcherProvider.getMatcherSerializableUID(ruleClass), matcherTagList);
        }
        nbt.putLongArray("TypesList", typesList);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        int version = nbt.getInt("Version");
        if (version < 8) {
            ExDepotMod.LOGGER.warn("Depots below version 8 are not supported. Skipping depot of version {}", version);
        } else {
            deserializeV8(nbt, version);
        }
    }

    private void deserializeV8(CompoundTag nbt, int version) {
        SortingRuleProvider matcherProvider = new SortingRuleProvider();
        for (long serializableUID : nbt.getLongArray("TypesList")) {
            ListTag matcherTagList = nbt.getList("TagList"+serializableUID, Tag.TAG_COMPOUND);
            for (int i = 0; i < matcherTagList.size(); i++)
            {
                CompoundTag matcherTag = matcherTagList.getCompound(i);
                AbstractSortingRule matcher = matcherProvider.loadMatcher(serializableUID, matcherTag, version);
                addRule(matcher);
            }
        }
    }
}
