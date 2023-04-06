package bike.guyona.exdepot.sortingrules.item;

import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemSortingRule extends AbstractSortingRule {
    String itemId;

    public ItemSortingRule(String itemId) {
        this.itemId = itemId;
    }

    public ItemSortingRule(CompoundTag nbt, int version) {
        this.load(nbt, version);
    }

    @Override
    public void save(CompoundTag nbt) {
        nbt.putString("ItemId", itemId);
    }

    @Override
    public AbstractSortingRule load(CompoundTag nbt, int version) {
        itemId = nbt.getString("ItemId");
        return this;
    }

    @Override
    public int hashCode() {
        return itemId.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ItemSortingRule) {
            return ((ItemSortingRule) other).itemId.equals(itemId);
        }
        return false;
    }

    @Override
    public Component getDisplayName() {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
        if (item == null) {
            return new TranslatableComponent("exdepot.depot.name.notfound");
        }
        return item.getName(item.getDefaultInstance());
    }

    @Override
    public void draw(int left, int top, float zLevel) {

    }

    @Override
    public Component getTypeDisplayName() {
        return new TranslatableComponent("exdepot.depot.type.item");
    }

    @Override
    public byte[] toBytes() {
        return itemId.getBytes();
    }

    public String getItemId() {
        return itemId;
    }
}
