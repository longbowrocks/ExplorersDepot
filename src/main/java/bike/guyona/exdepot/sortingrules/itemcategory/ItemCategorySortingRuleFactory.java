package bike.guyona.exdepot.sortingrules.itemcategory;

import bike.guyona.exdepot.helpers.AccessHelpers;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.AbstractSortingRuleFactory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

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
        ItemGroup tab = AccessHelpers.getCreativeTab(stack.getItem());
        if (tab != null) {
            return new ItemCategorySortingRule(AccessHelpers.getTabIndex(tab));
        }
        return null;
    }

    @Override
    public AbstractSortingRule fromBytes(ByteBuffer bbuf, int version) {
        if (version <= 7) {
            return fromBytesV7(bbuf, version);
        }
        return fromBytesV8(bbuf,version);
    }

    @Override
    public List<? extends AbstractSortingRule> getAllRules() {
        List<ItemCategorySortingRule> ruleList = new ArrayList<>();
        for (ItemGroup tab:ItemGroup.GROUPS) {
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

    private AbstractSortingRule fromBytesV7(ByteBuffer bbuf, int version) {
        int categoryLength = bbuf.getInt();
        byte[] catBuf = new byte[categoryLength];
        bbuf.get(catBuf);
        String catLabel = new String(catBuf, StandardCharsets.UTF_8);
        for (ItemGroup tab : ItemGroup.GROUPS) {
            if (AccessHelpers.getTabLabel(tab).equals(catLabel)) {
                return new ItemCategorySortingRule(AccessHelpers.getTabIndex(tab));
            }
        }
        return null;
    }

    private AbstractSortingRule fromBytesV8(ByteBuffer bbuf, int version) {
        int category = bbuf.getInt();
        return new ItemCategorySortingRule(category);
    }
}
