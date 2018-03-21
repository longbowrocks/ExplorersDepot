package bike.guyona.exdepot.helpers;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

public class AccessHelpers {
    public static Field buttonListField;
    private static Field upperChestField;
    private static Field lowerChestField;

    public static void setupButtonListAccessor() {
        buttonListField = ReflectionHelper.findField(GuiScreen.class, "buttonList", "field_146292_n");
    }

    public static void setupChestAccessors() {
        upperChestField = ReflectionHelper.findField(InventoryLargeChest.class, "upperChest", "field_70477_b");
        lowerChestField = ReflectionHelper.findField(InventoryLargeChest.class, "lowerChest", "field_70478_c");
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
}
