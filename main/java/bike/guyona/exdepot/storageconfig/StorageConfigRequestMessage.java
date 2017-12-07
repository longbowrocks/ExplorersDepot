package bike.guyona.exdepot.storageconfig;

import bike.guyona.exdepot.storageconfig.capability.StorageConfig;
import bike.guyona.exdepot.storageconfig.capability.StorageConfigProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static bike.guyona.exdepot.ExDepotMod.proxy;

/**
 * Created by longb on 12/5/2017.
 */
public class StorageConfigRequestMessage implements IMessage {
    public StorageConfigRequestMessage(){}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    public static class StorageConfigRequestMessageHandler implements IMessageHandler<StorageConfigRequestMessage, StorageConfigResponseMessage> {
        @Override
        public StorageConfigResponseMessage onMessage(StorageConfigRequestMessage message, MessageContext ctx) {
            // This is the player the packet was sent to the server from
            EntityPlayerMP serverPlayer = ctx.getServerHandler().playerEntity;
            TileEntityChest smallChest = null;
            // Get chests being configured.
            if (serverPlayer.openContainer != null && serverPlayer.openContainer instanceof ContainerChest){
                ContainerChest containerChest = (ContainerChest) serverPlayer.openContainer;
                System.out.println("Config message should be associated with chest: "+containerChest.getLowerChestInventory().toString());
                if (containerChest.getLowerChestInventory() instanceof TileEntityChest) {
                    smallChest = (TileEntityChest) containerChest.getLowerChestInventory();
                }else if (containerChest.getLowerChestInventory() instanceof InventoryLargeChest) {
                    InventoryLargeChest largeChest = (InventoryLargeChest) containerChest.getLowerChestInventory();
                    if (largeChest.upperChest instanceof TileEntityChest){
                        smallChest = (TileEntityChest) largeChest.upperChest;
                    }
                }else {
                    System.out.println("That's weird. We have a GUI open for a "+containerChest.getLowerChestInventory().toString());
                }
            }
            synchronized (proxy) {// Let's be real IntelliJ, you and I both know the proxy reference won't change.
                if (smallChest != null) {
                    StorageConfig conf = smallChest.getCapability(StorageConfigProvider.STORAGE_CONFIG_CAPABILITY, null);
                    return new StorageConfigResponseMessage(conf);
                } else {
                    System.out.println("How did this message get sent?");
                }
            }
            return new StorageConfigResponseMessage(new StorageConfig());
        }
    }
}
