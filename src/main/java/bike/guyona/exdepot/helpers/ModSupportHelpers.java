package bike.guyona.exdepot.helpers;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.api.IExDepotContainer;
import bike.guyona.exdepot.api.IExDepotGui;
import bike.guyona.exdepot.api.IExDepotTileEntity;
import bike.guyona.exdepot.config.ExDepotConfig;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * Vanilla Supported:
 * Brewing Stand
 * Chest
 * Dispenser
 * Double Chest
 * Dropper
 * Furnace
 * Hopper
 * Shulker Box
 *
 * Vanilla Denied:
 * Beacon
 * Enchanting Table
 * Horse Chest
 * Workbench
 * Anvil
 * Ender Chest
 * Player Inventory
 */
public class ModSupportHelpers {
    public static final CreativeTabs[] DISALLOWED_CATEGORIES = {
            CreativeTabs.SEARCH,
            CreativeTabs.INVENTORY
    };

    public static TileEntity getTileEntityFromBlockPos(BlockPos pos, World world) {
        if (pos.getY() < 0) {
            LOGGER.info("{} is not a good position", pos);
            return null;
        }
        TileEntity possibleChest = world.getTileEntity(pos);
        if (possibleChest == null) {
            LOGGER.info("{} has no chest", pos);
            return null;
        }
        return possibleChest;
    }

    public static boolean isTileEntitySupported(TileEntity tileEntity, boolean canCheckCapabilities) {
        return true;
    }
}
