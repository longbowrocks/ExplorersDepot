package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.helpers.GuiHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ItemSortingRule extends AbstractSortingRule {
    String itemId;
    Integer itemDamage;
    private ItemStack itemCache;

    ItemSortingRule(String itemId, int itemDamage) {
        this.itemId = itemId;
        this.itemDamage = itemDamage;
    }

    ItemSortingRule(ItemStack stack) {
        this.itemId = stack.getItem().getRegistryName().toString();
        if (stack.getHasSubtypes()) {
            this.itemDamage = stack.getItemDamage();
        } else {
            this.itemDamage = 0;
        }
    }

    ItemStack getItem() {
        if (itemCache == null) {
            Item item = Item.getByNameOrId(itemId);
            if (item != null) {
                itemCache = new ItemStack(item, 1, itemDamage);
            }
        }
        return itemCache;
    }

    @Override
    public int hashCode() {
        return itemId.hashCode() + itemDamage;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof ItemSortingRule &&
                itemId.equals(((ItemSortingRule) other).itemId) &&
                itemDamage.equals(((ItemSortingRule) other).itemDamage));
    }

    @Override
    public boolean matches(Object thing) {
        if (thing instanceof ItemSortingRule) {
            return equals(thing);
        } else if (thing instanceof ItemStack) {
            ItemStack stack = (ItemStack) thing;
            return itemId.equals(stack.getItem().getRegistryName().toString()) &&
                    (!stack.getHasSubtypes() || itemDamage == stack.getItemDamage());
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return getItem().getDisplayName();
    }

    @Override
    public void draw(int left, int top, float zLevel) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack stack = getItem();
        GuiHelpers.drawItem(left,
                top, stack, mc.fontRendererObj);
        mc.fontRendererObj.drawString(
                getDisplayName(),
                left + StorageConfigGui.ICON_WIDTH,
                top + 5,
                0xFFFFFF);
    }

    @Override
    public String getTypeDisplayName() {
        return "Item";
    }

    @Override
    public byte[] toBytes() {
        byte[] idBytes = itemId.getBytes(StandardCharsets.UTF_8);
        ByteBuffer outBuf = ByteBuffer.allocate(Integer.SIZE / 8 + idBytes.length + Integer.SIZE / 8);
        outBuf.putInt(idBytes.length);
        outBuf.put(idBytes);
        outBuf.putInt(itemDamage);
        return outBuf.array();
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof ItemSortingRule) {
            ItemSortingRule t = (ItemSortingRule) o;
            if (itemId.equals(t.itemId)) {
                return itemDamage.compareTo(t.itemDamage);
            }
            return itemId.compareTo(t.itemId);
        } else {
            throw new ClassCastException(String.format("object being compared is not a %s", this.getClass().toString()));
        }
    }
}
