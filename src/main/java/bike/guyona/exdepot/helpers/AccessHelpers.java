package bike.guyona.exdepot.helpers;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

public class AccessHelpers {
    private static Field buttonListField;
    private static Field upperChestField;
    private static Field lowerChestField;
    private static Field creativeTabField;

    public static void setupClientAccessors() {
        buttonListField = ReflectionHelper.findField(GuiScreen.class, "buttonList", "field_146292_n");
    }

    public static void setupCommonAccessors() {
        upperChestField = ReflectionHelper.findField(InventoryLargeChest.class, "upperChest", "field_70477_b");
        lowerChestField = ReflectionHelper.findField(InventoryLargeChest.class, "lowerChest", "field_70478_c");
        creativeTabField = ReflectionHelper.findField(Item.class, "tabToDisplayOn", "field_77701_a");
    }

    @SuppressWarnings("unchecked")
    public static List<GuiButton> getButtonList(GuiScreen gui) {
        try {
            return (List<GuiButton>) buttonListField.get(gui);
        } catch (IllegalAccessException e) {
            LOGGER.error("Couldn't access buttonList");
            e.printStackTrace();
        }
        return null;
    }

    static TileEntity getUpperChest(InventoryLargeChest fieldHolder) {
        try {
            return (TileEntity) upperChestField.get(fieldHolder);
        } catch (IllegalAccessException e) {
            LOGGER.error("Couldn't access upperChest");
            e.printStackTrace();
        }
        return null;
    }

    static TileEntity getLowerChest(InventoryLargeChest fieldHolder) {
        try {
            return (TileEntity) lowerChestField.get(fieldHolder);
        } catch (IllegalAccessException e) {
            LOGGER.error("Couldn't access lowerChest");
            e.printStackTrace();
        }
        return null;
    }

    // Item.getCreativeTab is client only (?!), and nobody puts one item on multiple creative tabs, so use this in place
    // of Item.getCreativeTabs (which calls Item.getCreativeTab by default).
    public static CreativeTabs getCreativeTab(Item item) {
        try {
            return (CreativeTabs) creativeTabField.get(item);
        } catch (IllegalAccessException e) {
            LOGGER.error("Couldn't access creativeTab");
            e.printStackTrace();
        }
        return null;
    }
}
