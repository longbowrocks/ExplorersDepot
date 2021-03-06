package bike.guyona.exdepot.sortingrules.mod;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.AbstractSortingRuleFactory;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static bike.guyona.exdepot.ExDepotMod.proxy;

public class ModSortingRuleFactory extends AbstractSortingRuleFactory {
    @Override
    public AbstractSortingRule fromItemStack(ItemStack stack) {
        if (stack.getItem().getRegistryName() == null) {
            return null;
        }
        return new ModSortingRule(stack.getItem().getRegistryName().getResourceDomain());
    }

    @Override
    public AbstractSortingRule fromBytes(ByteBuffer bbuf, int version) {
        int modIdLen = bbuf.getInt();
        byte[] modIdBuf = new byte[modIdLen];
        bbuf.get(modIdBuf);
        String modId = new String(modIdBuf, StandardCharsets.UTF_8);
        ModSortingRule rule = new ModSortingRule(modId);
        if (rule.getMod() == null) {
            ExDepotMod.LOGGER.error("Mod {} is no longer registered. Deleting rule.", modId);
            return null;
        }
        return rule;
    }

    @Override
    public List<? extends AbstractSortingRule> getAllRules() {
        List <ModSortingRule> allRules = new ArrayList<>();
        for(String modId : proxy.modsAndCategoriesThatRegisterItems.keySet()) {
            allRules.add(new ModSortingRule(modId));
        }
        return allRules;
    }

    @Override
    public List<TileEntity> getMatchingChests(ItemStack item, Map<? extends AbstractSortingRule, List<TileEntity>> chestsMap) {
        return null;
    }
}
