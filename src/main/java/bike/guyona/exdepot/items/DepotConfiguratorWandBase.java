package bike.guyona.exdepot.items;

import bike.guyona.exdepot.ExDepotMod;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;


import static bike.guyona.exdepot.ExDepotMod.*;

public class DepotConfiguratorWandBase extends Item {
    public DepotConfiguratorWandBase(Properties properties) {
        super(properties.stacksTo(1).tab(CreativeModeTab.TAB_TOOLS));
    }

    public static boolean isWand(Item item) {
        return AUTO_WAND_ITEM.get().equals(item) || GUI_WAND_ITEM.get().equals(item);
    }

    public enum Mode {
        AUTO_CONFIGURE,
        GUI_CONFIGURE;

        public static Mode getMode(DepotConfiguratorWandBase item) {
            if (AUTO_WAND_ITEM.get().equals(item)) {
                return AUTO_CONFIGURE;
            } else if (GUI_WAND_ITEM.get().equals(item)) {
                return GUI_CONFIGURE;
            }
            LOGGER.error("Got mode for unrecognized item {}", item);
            return AUTO_CONFIGURE;
        }

        public DepotConfiguratorWandBase getItem() {
            return switch (this) {
                case AUTO_CONFIGURE -> (AutoDepotConfiguratorWandItem)AUTO_WAND_ITEM.get();
                case GUI_CONFIGURE -> (GuiDepotConfiguratorWandItem)GUI_WAND_ITEM.get();
            };
        }
    }
}
