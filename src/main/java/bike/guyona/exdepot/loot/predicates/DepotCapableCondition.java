package bike.guyona.exdepot.loot.predicates;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import static bike.guyona.exdepot.helpers.ModSupportHelpers.isBlockEntityCompatible;

public class DepotCapableCondition implements LootItemCondition {
    public static final ResourceLocation ID = new ResourceLocation(Ref.MODID, "depot_capable");
    public static final DepotCapableCondition INSTANCE = new DepotCapableCondition();
    public static final ConditionSerializer SERIALIZER = new ConditionSerializer();

    public DepotCapableCondition(){}

    @Override
    public LootItemConditionType getType() {
        return ExDepotMod.DEPOT_CAPABLE_LOOT_CONDITION;
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.hasParam(LootContextParams.BLOCK_ENTITY) && isBlockEntityCompatible(lootContext.getParam(LootContextParams.BLOCK_ENTITY));
    }

    private static class ConditionSerializer implements Serializer<DepotCapableCondition> {
        @Override
        public void serialize(JsonObject json, DepotCapableCondition condition, JsonSerializationContext context) {

        }

        @Override
        public DepotCapableCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            return INSTANCE;
        }
    }
}
