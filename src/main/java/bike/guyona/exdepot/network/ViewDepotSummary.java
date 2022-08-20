package bike.guyona.exdepot.network;

import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.helpers.ChestFullness;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ViewDepotSummary(@NotNull BlockPos loc, @NotNull String modId, boolean isSimpleDepot, @NotNull ChestFullness chestFullness) {
    public static ViewDepotSummary fromDepot(BlockEntity chest, IDepotCapability cap) {
        BlockPos loc = chest.getBlockPos();
        Set<ModSortingRule> modRules = cap.getRules(ModSortingRule.class);
        Optional<String> modIdOptional = modRules.stream().map(ModSortingRule::getModId).findFirst();
        String modId = modIdOptional.orElse("");
        boolean isSimpleDepot = modRules.size() == 1 && cap.size() == 1;
        ChestFullness chestFullness = ChestFullness.getChestFullness(chest);
        return new ViewDepotSummary(loc, modId, isSimpleDepot, chestFullness);
    }
}
