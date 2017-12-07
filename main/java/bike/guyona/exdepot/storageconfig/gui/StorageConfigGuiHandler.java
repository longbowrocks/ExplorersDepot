package bike.guyona.exdepot.storageconfig.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

/**
 * Created by longb on 12/6/2017.
 */
public class StorageConfigGuiHandler implements IGuiHandler {
    public static final int STORAGE_CONFIG_GUI_ID = 0;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == STORAGE_CONFIG_GUI_ID) {
            if (player.openContainer != null && player.openContainer instanceof ContainerChest)
                return player.openContainer;
            LOGGER.error("CONGRATS MAN YOU FOUND A BUG. NOW HOW DID A PLAYER ACTIVATE THIS GUI WITHOUT OPENING A CHEST?");
            return null;
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == STORAGE_CONFIG_GUI_ID)
            return new StorageConfigGui();
        return null;
    }
}
