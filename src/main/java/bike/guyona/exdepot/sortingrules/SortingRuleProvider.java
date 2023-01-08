package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.sortingrules.item.ItemSortingRule;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class SortingRuleProvider {

    static final long modVersionUID = 30;
    static final long itemVersionUID = 40;

    public AbstractSortingRule loadMatcher(long serializableUID, CompoundTag matcherTag, int version) throws RuleUIDMapException {
        if (serializableUID == modVersionUID) {
            return new ModSortingRule(matcherTag, version);
        } else if (serializableUID == itemVersionUID) {
            return new ItemSortingRule(matcherTag, version);
        }
        throw new RuleUIDMapException("The serialization ID %d has no associated class".formatted(serializableUID));
    }

    public long getMatcherSerializableUID(Class<? extends AbstractSortingRule> matcherClass) throws RuleUIDMapException {
        if (matcherClass.equals(ModSortingRule.class)) {
            return modVersionUID;
        } else if (matcherClass.equals(ItemSortingRule.class)) {
            return itemVersionUID;
        }
        throw new RuleUIDMapException("The class %s has no associated serialization ID".formatted(matcherClass));
    }

    public <V extends AbstractSortingRule> V getRule(ItemStack stack, Class<V> type) {
        ResourceLocation res = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (res == null) {
            ExDepotMod.LOGGER.error("Impossible: you shouldn't be able to make an ItemStack for an unregistered item.");
            return null;
        }
        if (type.equals(ModSortingRule.class)) {
            return (V)new ModSortingRule(res.getNamespace());
        }
        else if (type.equals(ItemSortingRule.class)) {
            return (V)new ItemSortingRule(res.toString());
        }
        return null;
    }
}
