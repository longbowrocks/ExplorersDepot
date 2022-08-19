package bike.guyona.exdepot.capabilities;

import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Set;


public interface IDepotCapability extends INBTSerializable<CompoundTag> {
    boolean isEmpty();

    <T extends AbstractSortingRule> void addRule(T rule) ;

    @NotNull
    <T extends AbstractSortingRule> Set<T> getRules(Class<T> ruleClass);

    Set<Class<? extends AbstractSortingRule>> getRuleClasses();

    void copyFrom(IDepotCapability cap);

    int size();
}
