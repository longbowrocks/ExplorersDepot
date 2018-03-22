package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.helpers.AccessHelpers;
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
        CreativeTabs tab = AccessHelpers.getCreativeTab(stack.getItem());
        if (tab != null) {
            return new ItemCategorySortingRule(tab.getTabIndex());
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

    private AbstractSortingRule fromBytesV7(ByteBuffer bbuf, int version) {
        int categoryLength = bbuf.getInt();
        byte[] catBuf = new byte[categoryLength];
        bbuf.get(catBuf);
        String catLabel = new String(catBuf, StandardCharsets.UTF_8);
        for (CreativeTabs tab : CreativeTabs.CREATIVE_TAB_ARRAY) {
            if (AccessHelpers.getTabLabel(tab).equals(catLabel)) {
                return new ItemCategorySortingRule(tab.getTabIndex());
            }
        }
        return null;
    }

    private AbstractSortingRule fromBytesV8(ByteBuffer bbuf, int version) {
        int category = bbuf.getInt();
        return new ItemCategorySortingRule(category);
    }
}
