package bike.guyona.exdepot.helpers;

import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class is used to speed up sorting items into containers.
 *
 * Normally, for each item you would need to check each rule of each container in the area.
 * That's num_items*num_rules*num_containers, or O(n^3)
 *
 * With this helper, for each item you check whether it maps to one or more containers.
 * That's num_items*1, or O(n)
 */
public class DepotRouter <V extends AbstractSortingRule> {
    Map<V, List<BlockEntity>> ruleMap;

    public DepotRouter() {
        ruleMap = new HashMap<>();
    }

    public void addRules(Set<V> rules, BlockEntity block) {
        for (V rule : rules) {
            ruleMap.computeIfAbsent(rule, (k) -> new ArrayList<>());
            ruleMap.get(rule).add(block);
        }
    }

    @NotNull
    public List<BlockEntity> getDepots(V rule) {
        return ruleMap.getOrDefault(rule, new ArrayList<>());
    }
}
