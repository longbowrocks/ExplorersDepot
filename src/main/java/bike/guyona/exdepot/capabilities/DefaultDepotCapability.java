package bike.guyona.exdepot.capabilities;


import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.RuleUIDMapException;
import bike.guyona.exdepot.sortingrules.SortingRuleProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DefaultDepotCapability implements IDepotCapability {
    private static final int VERSION = 8;
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
            Set<AbstractSortingRule> copiedRules = new HashSet<>(cap.getRules(ruleClass));
            replaceRules(ruleClass, copiedRules);
        }
    }

    @Override
    public int size() {
        return rulesByType.values().stream().map(Set::size).reduce(Integer::sum).orElse(0);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultDepotCapability d)) {
            return false;
        }
        return rulesByType.equals(d.rulesByType);
    }

    @Override
    public CompoundTag serializeNBT() {
        SortingRuleProvider matcherProvider = new SortingRuleProvider();
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("Version", VERSION);
        List<Long> typesList = new ArrayList<>();
        for (Class<? extends AbstractSortingRule> ruleClass : getRuleClasses()) {
            long serializationUID;
            try {
                serializationUID = matcherProvider.getMatcherSerializableUID(ruleClass);
            } catch (RuleUIDMapException e) {
                ExDepotMod.LOGGER.error("Cannot persist matching rules: %s".formatted(e.getDetails()));
                continue;
            }
            typesList.add(serializationUID);
            ListTag matcherTagList = new ListTag();
            for (AbstractSortingRule matcher : getRules(ruleClass)) {
                CompoundTag matcherTag = new CompoundTag();
                matcher.save(matcherTag);
                matcherTagList.add(matcherTag);
            }
            nbt.put("TagList"+serializationUID, matcherTagList);
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
                try {
                    AbstractSortingRule matcher = matcherProvider.loadMatcher(serializableUID, matcherTag, version);
                    addRule(matcher);
                } catch (RuleUIDMapException e) {
                    ExDepotMod.LOGGER.error("Cannot load matching rule: %s".formatted(e.getDetails()));
                    continue;
                }
            }
        }
    }
}
