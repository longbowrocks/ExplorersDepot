package bike.guyona.exdepot.loot;

import bike.guyona.exdepot.ExDepotMod;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DepotPickerUpperLootModifier extends LootModifier {
    protected DepotPickerUpperLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        ExDepotMod.LOGGER.info("I HAVE ENCOUNTERED THE LOOTERATOR");
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<DepotPickerUpperLootModifier> {
        @Override
        public DepotPickerUpperLootModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions) {
            return new DepotPickerUpperLootModifier(conditions);
        }

        @Override
        public JsonObject write(DepotPickerUpperLootModifier instance) {
            return makeConditions(instance.conditions);
        }
    }
}
