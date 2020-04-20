package bike.guyona.exdepot.items;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.network.StorageConfigRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.config.ExDepotConfig.addOrRemoveFromCompatList;
import static bike.guyona.exdepot.config.ExDepotConfig.compatListIngameConf;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.getItemHandler;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.isTileEntitySupported;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class ItemConfigWand extends Item {
    public ItemConfigWand(String name) {
        setUnlocalizedName(Ref.MODID + "." + name);
        setRegistryName(name);
        setCreativeTab(CreativeTabs.TOOLS);
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        // We're just going to open a GUI, so let's not do anything server side.
        if (!worldIn.isRemote) {
            return EnumActionResult.PASS;
        }
        TileEntity possibleChest = worldIn.getTileEntity(pos);
        if (possibleChest == null){
            LOGGER.info("No TileEntity found at {}", pos);
            return EnumActionResult.PASS;
        }

        if (compatListIngameConf) {
            return addChestToManuallySupported(possibleChest);
        } else {
            return triggerConfigChest(possibleChest, pos);
        }
    }

    private EnumActionResult addChestToManuallySupported(TileEntity possibleChest) {
        addOrRemoveFromCompatList(possibleChest);
        return EnumActionResult.SUCCESS;
    }

    private EnumActionResult triggerConfigChest(TileEntity possibleChest, BlockPos pos) {
        if (!isTileEntitySupported(possibleChest)){
            TextComponentTranslation myText = new TextComponentTranslation("exdepot.chatmessage.tileEntityNotSupported");
            myText.getStyle().setColor(TextFormatting.LIGHT_PURPLE);
            Minecraft.getMinecraft().player.sendMessage(myText);
            LOGGER.warn("{} is not supported in the current compatibility mode.", possibleChest);
            return EnumActionResult.PASS;
        }
        // Request existing storage config in order to initialize the storage config GUI
        ExDepotMod.NETWORK.sendToServer(new StorageConfigRequestMessage(pos));
        return EnumActionResult.SUCCESS;
    }
}
