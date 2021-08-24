package bike.guyona.exdepot.sortingrules.modwithitemcategory;

import bike.guyona.exdepot.event.EventHandler;
import bike.guyona.exdepot.helpers.AccessHelpers;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.AbstractSortingRuleFactory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModWithItemCategorySortingRuleFactory extends AbstractSortingRuleFactory {
    @Override
    public AbstractSortingRule fromItemStack(ItemStack stack) {
        if (stack.getItem().getRegistryName() == null) {
            return null;
        }

        ItemGroup tab = AccessHelpers.getCreativeTab(stack.getItem());
        if (tab != null) {
            return new ModWithItemCategorySortingRule(stack.getItem().getRegistryName().getNamespace(), AccessHelpers.getTabIndex(tab));
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
        List<ModWithItemCategorySortingRule> ruleList = new ArrayList<>();
        for(String modId : EventHandler.modsAndCategoriesThatRegisterItems.keySet()) {
            for (Integer tabId : EventHandler.modsAndCategoriesThatRegisterItems.get(modId)) {
                ruleList.add(new ModWithItemCategorySortingRule(modId, tabId));
            }
        }
        return ruleList;
    }

    @Override
    public List<TileEntity> getMatchingChests(ItemStack item, Map<? extends AbstractSortingRule, List<TileEntity>> chestsMap) {
        return null;
    }

    private AbstractSortingRule fromBytesV7(ByteBuffer bbuf, int version) {
        int modIdLength = bbuf.getInt();
        byte[] modIdBuf = new byte[modIdLength];
        bbuf.get(modIdBuf);
        String modId = new String(modIdBuf, StandardCharsets.UTF_8);

        int categoryLength = bbuf.getInt();
        byte[] catBuf = new byte[categoryLength];
        bbuf.get(catBuf);
        String catLabel = new String(catBuf, StandardCharsets.UTF_8);
        for (ItemGroup tab : ItemGroup.GROUPS) {
            if (AccessHelpers.getTabLabel(tab).equals(catLabel)) {
                return new ModWithItemCategorySortingRule(modId, AccessHelpers.getTabIndex(tab));
            }
        }
        return null;
    }

    private AbstractSortingRule fromBytesV8(ByteBuffer bbuf, int version) {
        int modIdLength = bbuf.getInt();
        byte[] modIdBuf = new byte[modIdLength];
        bbuf.get(modIdBuf);
        String modId = new String(modIdBuf, StandardCharsets.UTF_8);

        int category = bbuf.getInt();
        return new ModWithItemCategorySortingRule(modId, category);
    }
}
