package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.helpers.TrackableModCategoryPair;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static bike.guyona.exdepot.helpers.ModSupportHelpers.DISALLOWED_CATEGORIES;

public class ModSortingRuleFactory extends AbstractSortingRuleFactory {
    @Override
    public AbstractSortingRule fromItemStack(ItemStack stack) {
        if (stack.getItem().getRegistryName() == null) {
            return null;
        }
        return new ModSortingRule(stack.getItem().getRegistryName().getResourceDomain());
    }

    @Override
    public AbstractSortingRule fromBytes(ByteBuffer bbuf) {
        int modIdLen = bbuf.getInt();
        byte[] modIdBuf = new byte[modIdLen];
        bbuf.get(modIdBuf, bbuf.arrayOffset(), modIdLen);
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
        Loader loader = Loader.instance();
        for(ModContainer mod : loader.getModList()) {
            allRules.add(new ModSortingRule(mod));
        }
        return allRules;
    }

    @Override
    public List<TileEntity> getMatchingChests(ItemStack item, Map<? extends AbstractSortingRule, List<TileEntity>> chestsMap) {
        return null;
    }
}