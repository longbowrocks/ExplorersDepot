package bike.guyona.exdepot.helpers;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemShield;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

public class AccessHelpers {
    private static Field buttonListField;
    private static Field upperChestField;
    private static Field lowerChestField;
    private static Field creativeTabBaseField;
    private static Field creativeTabItemBlockField;
    private static Field tabLabelField;
    private static Field tabIndexField;
    private static Field lowerChestInventoryField;

    public static void setupClientAccessors() {
        buttonListField = ReflectionHelper.findField(GuiScreen.class, "buttonList", "field_146292_n");
    }

    public static void setupCommonAccessors() {
        upperChestField = ReflectionHelper.findField(InventoryLargeChest.class, "upperChest", "field_70477_b");
        lowerChestField = ReflectionHelper.findField(InventoryLargeChest.class, "lowerChest", "field_70478_c");
        creativeTabBaseField = ReflectionHelper.findField(Item.class, "tabToDisplayOn", "field_77701_a");
        creativeTabItemBlockField = ReflectionHelper.findField(Block.class, "displayOnCreativeTab", "field_149772_a");
        tabLabelField = ReflectionHelper.findField(CreativeTabs.class, "tabLabel", "field_78034_o");
        tabIndexField = ReflectionHelper.findField(CreativeTabs.class, "tabIndex", "field_78033_n");
        lowerChestInventoryField = ReflectionHelper.findField(CreativeTabs.class, "lowerChestInventory", "field_147015_w");
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

    static IInventory getLowerChestInventory(GuiChest fieldHolder) {
        try {
            return (IInventory) lowerChestInventoryField.get(fieldHolder);
        } catch (IllegalAccessException e) {
            LOGGER.error("Couldn't access lowerChestInventory");
            e.printStackTrace();
        }
        return null;
    }

    // Item.getCreativeTab is client only (?!), and nobody puts one item on multiple creative tabs, so use this in place
    // of Item.getCreativeTabs (which calls Item.getCreativeTab by default).
    public static CreativeTabs getCreativeTab(Item item) {
        try {
            if (item instanceof ItemShield) {
                return CreativeTabs.COMBAT;
            } else if (item instanceof ItemBanner) {
                return CreativeTabs.DECORATIONS;
            } else if (item instanceof ItemBlock) {
                return (CreativeTabs) creativeTabItemBlockField.get(((ItemBlock) item).block);
            } else {
                return (CreativeTabs) creativeTabBaseField.get(item);
            }
        } catch (IllegalAccessException e) {
            LOGGER.error("Couldn't access creativeTab");
            e.printStackTrace();
        }
        return null;
    }

    public static String getTabLabel(CreativeTabs tab) {
        try {
            return (String) tabLabelField.get(tab);
        } catch (IllegalAccessException e) {
            LOGGER.error("Couldn't access tabLabel");
            e.printStackTrace();
        }
        return null;
    }

    public static Integer getTabIndex(CreativeTabs tab) {
        try {
            return tabIndexField.getInt(tab);
        } catch (IllegalAccessException e) {
            LOGGER.error("Couldn't access tabIndex");
            e.printStackTrace();
        }
        return null;
    }
}
