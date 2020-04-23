package bike.guyona.exdepot.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

public class ItemRegistrar {
    private static List<Item> allMyItems;
    private static ItemConfigWand configWand;

    public ItemRegistrar() {
        allMyItems = Arrays.asList(
            configWand = new ItemConfigWand("storage_configuration_wand")
        );
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        for (Item item:allMyItems){
            event.getRegistry().register(item);
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerRenders() {
        ItemModelMesher modelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        for (Item item:allMyItems) {
            modelMesher.register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}
