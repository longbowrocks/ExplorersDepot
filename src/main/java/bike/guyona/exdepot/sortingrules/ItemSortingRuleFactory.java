package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.helpers.NbtHelpers;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.helpers.ItemLookupHelpers.getSubtypes;

public class ItemSortingRuleFactory extends AbstractSortingRuleFactory {
    @Override
    public AbstractSortingRule fromItemStack(ItemStack stack) {
        return new ItemSortingRule(stack);
    }

    @Override
    public AbstractSortingRule fromBytes(ByteBuffer bbuf, int version) {
        switch (version) {
            case 4:
                return fromBytesV4(bbuf);
            default:
                return fromBytesV5(bbuf);
        }
    }

    @Override
    public List<? extends AbstractSortingRule> getAllRules() {
        List<ItemSortingRule> ruleList = new ArrayList<>();
        for(Item item : Item.REGISTRY) {
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

    private AbstractSortingRule fromBytesV5(ByteBuffer bbuf) {
        int itemIdLength = bbuf.getInt();
        byte[] itemIdBuf = new byte[itemIdLength];
        bbuf.get(itemIdBuf);
        String itemId = new String(itemIdBuf, StandardCharsets.UTF_8);
        int itemDamage = bbuf.getInt();
        NBTTagCompound nbt = NbtHelpers.fromBytes(bbuf);
        return new ItemSortingRule(itemId, itemDamage, nbt);
    }
}
