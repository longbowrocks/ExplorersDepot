package bike.guyona.exdepot.network;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.capability.StorageConfigProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;

/**
 * Created by longb on 9/9/2017.
 */
public class StorageConfigCreateMessage implements IMessage, IMessageHandler<StorageConfigCreateMessage, IMessage> {
    private StorageConfig data;

    public StorageConfigCreateMessage(){}

    public StorageConfigCreateMessage(StorageConfig toSend) {
        data = toSend;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        byte[] bytes = data.toBytes();
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int objLength = buf.readInt();
        byte[] bytes = new byte[objLength];
        buf.readBytes(bytes);
        data = StorageConfig.fromBytes(bytes);
    }

    @Override
    public IMessage onMessage(StorageConfigCreateMessage message, MessageContext ctx) {
        // This is the player the packet was sent to the server from
        EntityPlayerMP serverPlayer = ctx.getServerHandler().playerEntity;
        Vector<TileEntityChest> smallChests = new Vector<>();
        // Get chests being configured.
        if (serverPlayer.openContainer != null && serverPlayer.openContainer instanceof ContainerChest){
            ContainerChest containerChest = (ContainerChest) serverPlayer.openContainer;
            LOGGER.info("Config message should be associated with chest: "+containerChest.getLowerChestInventory().toString());
            if (containerChest.getLowerChestInventory() instanceof TileEntityChest) {
                smallChests.add((TileEntityChest) containerChest.getLowerChestInventory());
            }else if (containerChest.getLowerChestInventory() instanceof InventoryLargeChest) {
                InventoryLargeChest largeChest = (InventoryLargeChest) containerChest.getLowerChestInventory();
                if (largeChest.upperChest instanceof TileEntityChest){
                    smallChests.add((TileEntityChest) largeChest.upperChest);
                }
                if (largeChest.lowerChest instanceof TileEntityChest){
                    smallChests.add((TileEntityChest) largeChest.lowerChest);
                }
            }else {
                LOGGER.warn("That's weird. We have a GUI open for a " + containerChest.getLowerChestInventory().toString());
            }
        }
        // Associate chests with received StorageConfig, and add to cache.
        serverPlayer.getServerWorld().addScheduledTask(() -> {
            synchronized (proxy) {// Let's be real IntelliJ, you and I both know the proxy reference won't change.
                for (TileEntityChest chest:smallChests) {
                    StorageConfig conf = chest.getCapability(StorageConfigProvider.STORAGE_CONFIG_CAPABILITY, null);
                    conf.copyFrom(message.data);
                }
            }
        });
        // No response packet
        return new StorageConfigCreateResponse();
    }
}
