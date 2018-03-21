package bike.guyona.exdepot.helpers;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

public class ItemLookupHelpers {
    // Minecraft only registers item subtypes on clients.
    @SideOnly(Side.CLIENT)
    public static List<ItemStack> getSubtypes(Item item) {
        List<ItemStack> allItemTypes = new ArrayList<>();
        CreativeTabs tab = AccessHelpers.getCreativeTab(item);
        if (tab != null) {
            NonNullList<ItemStack> subItems = NonNullList.create();
            item.getSubItems(item, tab, subItems);
            for (ItemStack stack:subItems) {
                if (!stack.isEmpty())
                    allItemTypes.add(stack);
            }
        }
        return allItemTypes;
    }
}
