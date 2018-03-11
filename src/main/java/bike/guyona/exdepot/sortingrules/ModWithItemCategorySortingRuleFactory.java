package bike.guyona.exdepot.sortingrules;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static bike.guyona.exdepot.ExDepotMod.proxy;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.DISALLOWED_CATEGORIES;

public class ModWithItemCategorySortingRuleFactory extends AbstractSortingRuleFactory {
    @Override
    public AbstractSortingRule fromItemStack(ItemStack stack) {
        if (stack.getItem().getRegistryName() == null) {
            return null;
        }
        for (CreativeTabs tab : stack.getItem().getCreativeTabs()) {
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
    public AbstractSortingRule fromBytes(ByteBuffer bbuf) {
        int modIdLength = bbuf.getInt();
        byte[] modIdBuf = new byte[modIdLength];
        bbuf.get(modIdBuf, bbuf.arrayOffset(), modIdLength);
        String modId = new String(modIdBuf, StandardCharsets.UTF_8);

        int categoryLength = bbuf.getInt();
        byte[] catBuf = new byte[categoryLength];
        bbuf.get(catBuf, bbuf.arrayOffset(), categoryLength);
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
