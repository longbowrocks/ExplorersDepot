package bike.guyona.exdepot.network.viewdepots;

import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.helpers.ChestFullness;
import bike.guyona.exdepot.helpers.ModSupportHelpers;
import bike.guyona.exdepot.sortingrules.item.ItemSortingRule;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import com.mojang.math.Vector3d;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record ViewDepotSummary(@NotNull Vector3d loc, @NotNull String modId, boolean isSimpleDepot, @NotNull ChestFullness chestFullness) {
    public static ViewDepotSummary fromDepot(BlockEntity chest, IDepotCapability cap) {
        Vector3d loc = ModSupportHelpers.viewDepotDrawLocation(chest);
        String modId = getBestMod(cap);
        boolean isSimpleDepot = isModSummaryComplete(cap, modId);
        ChestFullness chestFullness = ChestFullness.getChestFullness(chest);
        return new ViewDepotSummary(loc, modId, isSimpleDepot, chestFullness);
    }

    /**
     * Look over all SortingRules in a depot, and identify the mod that best represents that depot.
     */
    private static String getBestMod(IDepotCapability cap) {
        Set<ItemSortingRule> itemRules = cap.getRules(ItemSortingRule.class);
        Map<String, Integer> ruleCountByMod = new HashMap<>();
        for (ItemSortingRule itemRule : itemRules) {
            String modId = Arrays.stream(itemRule.getItemId().split(":")).findFirst().get();
            ruleCountByMod.put(modId, ruleCountByMod.getOrDefault(modId, 0) + 1);
        }
        if (ruleCountByMod.size() > 0) {
            Optional<Map.Entry<String,Integer>> mostUsedMod = ruleCountByMod.entrySet().stream().reduce((e1,e2) -> e1.getValue() > e2.getValue() ? e1 : e2);
            return mostUsedMod.get().getKey();
        }
        Set<ModSortingRule> modRules = cap.getRules(ModSortingRule.class);
        Optional<String> modIdOptional = modRules.stream().map(ModSortingRule::getModId).findFirst();
        return modIdOptional.orElse("");
    }

    /**
     * If a single mod perfectly represents the contents of a depot, return true. Otherwise false.
     */
    private static boolean isModSummaryComplete(IDepotCapability cap, String modId) {
        Set<ModSortingRule> modRules = cap.getRules(ModSortingRule.class);
        return (modRules.size() == 1 && cap.size() == 1) || cap.size() == 0;
    }
}
