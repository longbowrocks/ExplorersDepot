package bike.guyona.exdepot.sortingrules.item;

import bike.guyona.exdepot.helpers.NbtHelpers;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.AbstractSortingRuleFactory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.registries.ForgeRegistries;

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
    public AbstractSortingRule fromBytes(ByteBuffer bbuf, int version) {
        if (version <= 5) {
            return fromBytesV4(bbuf);
        }
        return fromBytesV6(bbuf);
    }

    @Override
    public List<? extends AbstractSortingRule> getAllRules() {
        List<ItemSortingRule> ruleList = new ArrayList<>();
        for(Item item : ForgeRegistries.ITEMS) {
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

    private AbstractSortingRule fromBytesV4(ByteBuffer bbuf) {
        int itemIdLength = bbuf.getInt();
        byte[] itemIdBuf = new byte[itemIdLength];
        bbuf.get(itemIdBuf);
        String itemId = new String(itemIdBuf, StandardCharsets.UTF_8);
        int itemDamage = bbuf.getInt();
        return new ItemSortingRule(itemId, itemDamage, null);
    }

    private AbstractSortingRule fromBytesV6(ByteBuffer bbuf) {
        int itemIdLength = bbuf.getInt();
        byte[] itemIdBuf = new byte[itemIdLength];
        bbuf.get(itemIdBuf);
        String itemId = new String(itemIdBuf, StandardCharsets.UTF_8);
        int itemDamage = bbuf.getInt();
        CompoundNBT nbt = NbtHelpers.fromBytes(bbuf);
        return new ItemSortingRule(itemId, itemDamage, nbt);
    }
}
