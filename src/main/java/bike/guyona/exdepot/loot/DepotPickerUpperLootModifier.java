package bike.guyona.exdepot.loot;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.events.EventHandler;
import bike.guyona.exdepot.items.DepotConfiguratorWandBase;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static bike.guyona.exdepot.ExDepotMod.CAPABILITY_CACHE_KEY;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.isBlockEntityCompatible;

public class DepotPickerUpperLootModifier extends LootModifier {
    public static final Codec<DepotPickerUpperLootModifier> CODEC = new Codec<DepotPickerUpperLootModifier>() {
        @Override
        public <T> DataResult<Pair<DepotPickerUpperLootModifier, T>> decode(DynamicOps<T> ops, T input) {
            Optional<Pair<LootItemCondition[], T>> res = LOOT_CONDITIONS_CODEC.decode(ops, input).result();
            if (res.isEmpty()) {
                return DataResult.error("Impossible: no result for conditions on DepotPickerUpperLootModifier");
            }
            LootItemCondition[] conditions = res.get().getFirst();
            if (conditions == null) {
                ExDepotMod.LOGGER.error("IMPOSSIBLE: I specified conditions in the json for this loot modifier, but they're not being passed on.");
                conditions = new LootItemCondition[]{};
            }
            return DataResult.success(Pair.of(new DepotPickerUpperLootModifier(conditions), ops.empty()));
        }

        @Override
        public <T> DataResult<T> encode(DepotPickerUpperLootModifier input, DynamicOps<T> ops, T prefix) {
            return LOOT_CONDITIONS_CODEC.encode(input.conditions, ops, prefix);
        }
    };

    protected DepotPickerUpperLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!(context.hasParam(LootContextParams.TOOL) && context.hasParam(LootContextParams.BLOCK_ENTITY))) {
            ExDepotMod.LOGGER.error("Impossible: loot event {} passes the match_tool and depot_capable conditions, but is missing either a tool or blockEntity", generatedLoot);
            return generatedLoot;
        }
        ItemStack tool = context.getParam(LootContextParams.TOOL);
        if (!DepotConfiguratorWandBase.isWand(tool.getItem())) {
            ExDepotMod.LOGGER.error("Impossible: loot event {} passes the match_tool condition without using the wand", generatedLoot);
            return generatedLoot;
        }
        BlockEntity entity = context.getParam(LootContextParams.BLOCK_ENTITY);
        if (!isBlockEntityCompatible(entity)) {
            ExDepotMod.LOGGER.error("Impossible: loot event {} passes the depot_capable condition without being depot capable", generatedLoot);
            return generatedLoot;
        }
        if (generatedLoot.size() != 1) {
            ExDepotMod.LOGGER.warn("{} itemized to {} items instead of 1 item", entity, generatedLoot.size());
            return generatedLoot;
        }
        if (!(context.getParam(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player)) {
            ExDepotMod.LOGGER.debug("A non-player harvested {}, so we're not going to keep the depot.", entity);
            return generatedLoot;
        }
        CompoundTag depotCache = EventHandler.getDepotCache(entity.getBlockPos(), player.getId());
        if (depotCache == null) {
            return generatedLoot;
        }
        generatedLoot.get(0).addTagElement(CAPABILITY_CACHE_KEY, depotCache);
        ExDepotMod.LOGGER.debug("Attached depot to {}", generatedLoot.get(0));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
