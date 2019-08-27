package bike.guyona.exdepot.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemConfigWand extends Item {
    public ItemConfigWand(String name) {
        setUnlocalizedName(name);
        setRegistryName(name);
        setCreativeTab(CreativeTabs.TOOLS);
    }
}
