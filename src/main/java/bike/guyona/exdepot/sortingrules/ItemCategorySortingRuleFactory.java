package bike.guyona.exdepot.sortingrules;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static bike.guyona.exdepot.helpers.ModSupportHelpers.DISALLOWED_CATEGORIES;

public class ItemCategorySortingRuleFactory extends AbstractSortingRuleFactory {
    @Override
    public AbstractSortingRule fromItemStack(ItemStack stack) {
        for (CreativeTabs tab : stack.getItem().getCreativeTabs()) {
            NonNullList<ItemStack> subItems = NonNullList.create();
            stack.getItem().getSubItems(stack.getItem(), tab, subItems);
            for (ItemStack sub: subItems) {
                if (stack.getItemDamage() == sub.getItemDamage()) {
                    return new ItemCategorySortingRule(tab.getTabLabel());
                }
            }
        }
        return null;
    }

    @Override
    public AbstractSortingRule fromBytes(ByteBuffer bbuf, int version) {
        int categoryLength = bbuf.getInt();
        byte[] catBuf = new byte[categoryLength];
        bbuf.get(catBuf, bbuf.arrayOffset(), categoryLength);
        String catLabel = new String(catBuf, StandardCharsets.UTF_8);
        return new ItemCategorySortingRule(catLabel);
    }

    @Override
    public List<? extends AbstractSortingRule> getAllRules() {
        List<ItemCategorySortingRule> ruleList = new ArrayList<>();
        for (CreativeTabs tab:CreativeTabs.CREATIVE_TAB_ARRAY) {
            if (Arrays.asList(DISALLOWED_CATEGORIES).contains(tab)) {
                continue;
            }
            ruleList.add(new ItemCategorySortingRule(tab));
        }
        return ruleList;
    }

    @Override
    public List<TileEntity> getMatchingChests(ItemStack item, Map<? extends AbstractSortingRule, List<TileEntity>> chestsMap) {
        return null;
    }
}
