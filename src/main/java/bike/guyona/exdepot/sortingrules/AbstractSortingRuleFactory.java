package bike.guyona.exdepot.sortingrules;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public abstract class AbstractSortingRuleFactory {
    public abstract AbstractSortingRule fromItemStack(ItemStack stack);

    public abstract AbstractSortingRule fromBytes(ByteBuffer bbuf);

    public abstract List<AbstractSortingRule> getAllRules();

    public abstract List<TileEntity> getMatchingChests(ItemStack item, Map<AbstractSortingRule, List<TileEntity>> chestsMap);
}
