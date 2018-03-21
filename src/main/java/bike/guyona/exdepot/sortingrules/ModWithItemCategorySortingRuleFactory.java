package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.helpers.AccessHelpers;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static bike.guyona.exdepot.ExDepotMod.proxy;

public class ModWithItemCategorySortingRuleFactory extends AbstractSortingRuleFactory {
    @Override
    public AbstractSortingRule fromItemStack(ItemStack stack) {
        if (stack.getItem().getRegistryName() == null) {
            return null;
        }

        CreativeTabs tab = AccessHelpers.getCreativeTab(stack.getItem());
        if (tab != null) {
            NonNullList<ItemStack> subItems = NonNullList.create();
            stack.getItem().getSubItems(stack.getItem(), tab, subItems);
            for (ItemStack sub: subItems) {
                if (stack.getItemDamage() == sub.getItemDamage()) {
                    return new ModWithItemCategorySortingRule(stack.getItem().getRegistryName().getResourceDomain(), tab.getTabLabel());
                }
            }
        }
        return null;
    }

    @Override
    public AbstractSortingRule fromBytes(ByteBuffer bbuf, int version) {
        int modIdLength = bbuf.getInt();
        byte[] modIdBuf = new byte[modIdLength];
        bbuf.get(modIdBuf);
        String modId = new String(modIdBuf, StandardCharsets.UTF_8);

        int categoryLength = bbuf.getInt();
        byte[] catBuf = new byte[categoryLength];
        bbuf.get(catBuf);
        String catLabel = new String(catBuf, StandardCharsets.UTF_8);
        return new ModWithItemCategorySortingRule(modId, catLabel);
    }

    @Override
    public List<? extends AbstractSortingRule> getAllRules() {
        List<ModWithItemCategorySortingRule> ruleList = new ArrayList<>();
        for(String modId : proxy.modsAndCategoriesThatRegisterItems.keySet()) {
            for (String tabId : proxy.modsAndCategoriesThatRegisterItems.get(modId)) {
                ruleList.add(new ModWithItemCategorySortingRule(modId, tabId));
            }
        }
        return ruleList;
    }

    @Override
    public List<TileEntity> getMatchingChests(ItemStack item, Map<? extends AbstractSortingRule, List<TileEntity>> chestsMap) {
        return null;
    }
}
