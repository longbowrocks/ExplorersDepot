package bike.guyona.exdepot.helpers;


import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TrackableItemStack implements Comparable{
    public final String itemId;
    public final Integer itemDamage;

    public TrackableItemStack(String itemId, int itemDamage) {
        this.itemId = itemId;
        this.itemDamage = itemDamage;
    }

    public TrackableItemStack(ItemStack stack) {
        this.itemId = stack.getItem().getRegistryName().toString();
        if (stack.getHasSubtypes()) {
            this.itemDamage = stack.getItemDamage();
        } else {
            this.itemDamage = 0;
        }
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof TrackableItemStack) {
            TrackableItemStack t = (TrackableItemStack) o;
            if (itemId.equals(t.itemId)) {
                return itemDamage.compareTo(t.itemDamage);
            }
            return itemId.compareTo(t.itemId);
        } else {
            throw new ClassCastException("object being compared is not a TrackableItemStack");
        }
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof TrackableItemStack &&
                itemId.equals(((TrackableItemStack) obj).itemId) &&
                itemDamage.equals(((TrackableItemStack) obj).itemDamage));
    }
}
