package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.helpers.GuiHelpers;
import bike.guyona.exdepot.helpers.NbtHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.helpers.ItemLookupHelpers.getSubtypes;

public class ItemSortingRule extends AbstractSortingRule {
    static final long serialVersionUID = 20;

    private final String itemId;
    private final Integer itemDamage;
    private final NBTTagCompound itemTags;
    private boolean useNbt = true;
    private ItemStack itemCache;

    ItemSortingRule(String itemId, int itemDamage, NBTTagCompound nbt) {
        this.itemId = itemId;
        this.itemDamage = itemDamage;
        this.itemTags = nbt;
    }

    ItemSortingRule(ItemStack stack) {
        this.itemId = stack.getItem().getRegistryName().toString();
        if (stack.getHasSubtypes()) {
            this.itemDamage = stack.getItemDamage();
        } else {
            this.itemDamage = 0;
        }
        // TODO Can't do this. There are way too many extra tags that could be added. Look through registry and find the item.
        itemTags = stack.getTagCompound();
    }

    ItemStack getItem() {
        if (itemCache == null) {
            Item item = Item.getByNameOrId(itemId);
            if (item != null) {
                for (ItemStack stack : getSubtypes(item)) {
                    if (matches(stack)) {
                        itemCache = stack;
                        return stack;
                    }
                }
            }
        }
        return itemCache;
    }

    public void setUseNbt(boolean useNbt) {
        itemCache = null; // might need to draw a different item
        this.useNbt = useNbt;
    }

    @Override
    public int hashCode() {
        //TODO: tags can't be in hashCode if I'm going to switch them in and out. make useNbt final and recreate rules?
        return itemId.hashCode() + itemDamage;// + (itemTags == null ? 0 : itemTags.hashCode());
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof ItemSortingRule &&
                itemId.equals(((ItemSortingRule) other).itemId) &&
                itemDamage.equals(((ItemSortingRule) other).itemDamage) &&
                (!useNbt || !((ItemSortingRule) other).useNbt ||
                        itemTags == null || itemTags.equals(((ItemSortingRule) other).itemTags)
                ));
    }

    @Override
    public boolean matches(Object thing) {
        if (thing instanceof ItemSortingRule) {
            return equals(thing);
        } else if (thing instanceof ItemStack) {
            ItemStack stack = (ItemStack) thing;
            return itemId.equals(stack.getItem().getRegistryName().toString()) &&
                    (!stack.getHasSubtypes() || itemDamage == stack.getItemDamage()) &&
                    (!useNbt || itemTags == null || itemTags.equals(stack.getTagCompound()));
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
        byte[] nbtArray = NbtHelpers.toBytes(itemTags);

        ByteBuffer outBuf = ByteBuffer.allocate(Integer.SIZE / 8 + idBytes.length + Integer.SIZE / 8 + nbtArray.length);
        outBuf.putInt(idBytes.length);
        outBuf.put(idBytes);
        outBuf.putInt(itemDamage);
        outBuf.put(nbtArray);
        return outBuf.array();
    }
}
