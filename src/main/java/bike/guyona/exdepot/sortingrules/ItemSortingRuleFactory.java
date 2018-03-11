package bike.guyona.exdepot.sortingrules;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static bike.guyona.exdepot.helpers.ItemLookupHelpers.getSubtypes;

public class ItemSortingRuleFactory extends AbstractSortingRuleFactory {
    @Override
    public AbstractSortingRule fromItemStack(ItemStack stack) {
        return new ItemSortingRule(stack);
    }

    @Override
    public AbstractSortingRule fromBytes(ByteBuffer bbuf) {
        int itemIdLength = bbuf.getInt();
        byte[] itemIdBuf = new byte[itemIdLength];
        bbuf.get(itemIdBuf, bbuf.arrayOffset(), itemIdLength);
        String itemId = new String(itemIdBuf, StandardCharsets.UTF_8);

        int itemDamage = bbuf.getInt();

        return new ItemSortingRule(itemId, itemDamage);
    }

    @Override
    public List<? extends AbstractSortingRule> getAllRules() {
        List<ItemSortingRule> ruleList = new ArrayList<>();
        for(Item item : Item.REGISTRY) {
            for (ItemStack itemStack : getSubtypes(item)) {
                ruleList.add(new ItemSortingRule(itemStack));
            }
        }
        return ruleList;
    }

    @Override
    public List<TileEntity> getMatchingChests(ItemStack item, Map<? extends AbstractSortingRule, List<TileEntity>> chestsMap) {
        return null;
    }
}
