package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class SortingRuleProvider {
    public AbstractSortingRule loadMatcher(long serializableUID, CompoundTag matcherTag, int version) {
        return new ModSortingRule(matcherTag, version);
    }

    public long getMatcherSerializableUID(Class<? extends AbstractSortingRule> matcherClass) {
        return 30;
    }

    public <V extends AbstractSortingRule> V getRule(ItemStack stack, Class<V> type) {
        if (type.equals(ModSortingRule.class)) {
            ResourceLocation res = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (res == null) {
                ExDepotMod.LOGGER.error("Impossible: you shouldn't be able to make an ItemStack for an unregistered item.");
                return null;
            }
            return (V)new ModSortingRule(res.getNamespace());
        }
        return null;
    }
}
