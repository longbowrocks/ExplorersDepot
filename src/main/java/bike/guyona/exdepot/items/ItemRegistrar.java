package bike.guyona.exdepot.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

public class ItemRegistrar {
    private static List<Item> allMyItems;
    private static ItemConfigWand configWand;

    public static void init() {
        allMyItems = Arrays.asList(
            configWand = new ItemConfigWand("storage_configuration_wand")
        );
    }

    public static void registerItems() {
        for (Item item:allMyItems){
            GameRegistry.register(item);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerRenders() {
        ItemModelMesher modelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        for (Item item:allMyItems) {
            modelMesher.register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}
