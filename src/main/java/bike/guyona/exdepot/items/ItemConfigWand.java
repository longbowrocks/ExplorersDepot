package bike.guyona.exdepot.items;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.network.StorageConfigRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.config.ExDepotConfig.addOrRemoveFromCompatList;
import static bike.guyona.exdepot.config.ExDepotConfig.compatListIngameConf;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.isTileEntitySupported;

public class ItemConfigWand extends Item {
    public ItemConfigWand(Item.Properties properties) {
        super(properties.maxStackSize(1).group(ItemGroup.TOOLS));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        // We're just going to open a GUI, so let's not do anything server side.
        if (!context.getWorld().isRemote) {
            return ActionResultType.PASS;
        }
        TileEntity possibleChest = context.getWorld().getTileEntity(context.getPos());
        if (possibleChest == null){
            LOGGER.info("No TileEntity found at {}", context.getPos());
            return ActionResultType.FAIL;
        }

        if (compatListIngameConf) {
            return addChestToManuallySupported(possibleChest);
        } else {
            return triggerConfigChest(possibleChest, context.getPos());
        }
    }

    private ActionResultType addChestToManuallySupported(TileEntity possibleChest) {
        addOrRemoveFromCompatList(possibleChest);
        return ActionResultType.SUCCESS;
    }

    private ActionResultType triggerConfigChest(TileEntity possibleChest, BlockPos pos) {
        if (!isTileEntitySupported(possibleChest)){
            TranslationTextComponent myText = new TranslationTextComponent("exdepot.chatmessage.tileEntityNotSupported");
            myText.getStyle().setColor(TextFormatting.LIGHT_PURPLE);
            Minecraft.getInstance().player.sendMessage(myText);
            LOGGER.warn("{} is not supported in the current compatibility mode.", possibleChest);
            return ActionResultType.FAIL;
        }
        // Request existing storage config in order to initialize the storage config GUI
        ExDepotMod.NETWORK.sendToServer(new StorageConfigRequestMessage(pos));
        return ActionResultType.SUCCESS;
    }
}
