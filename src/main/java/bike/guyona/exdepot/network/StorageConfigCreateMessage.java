package bike.guyona.exdepot.network;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.capability.StorageConfigProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;
import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.getContainerTileEntities;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.getTileEntityFromBlockPos;

/**
 * Created by longb on 9/9/2017.
 */
public class StorageConfigCreateMessage implements IMessage, IMessageHandler<StorageConfigCreateMessage, IMessage> {
    private BlockPos chestPos;
    private StorageConfig data;

    public StorageConfigCreateMessage(){}

    public StorageConfigCreateMessage(StorageConfig toSend, BlockPos chestPosition) {
        data = toSend;
        chestPos = chestPosition;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(chestPos.getX());
        buf.writeInt(chestPos.getY());
        buf.writeInt(chestPos.getZ());

        byte[] bytes = data.toBytes();
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        chestPos = new BlockPos(x, y, z);

        int objLength = buf.readInt();
        byte[] bytes = new byte[objLength];
        buf.readBytes(bytes);
        data = StorageConfig.fromBytes(bytes);
    }

    @Override
    public IMessage onMessage(StorageConfigCreateMessage message, MessageContext ctx) {
        // This is the player the packet was sent to the server from
        EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
        // Associate chests with received StorageConfig, and add to cache.
        serverPlayer.getServerWorld().addScheduledTask(() -> {
            //noinspection SynchronizeOnNonFinalField
            synchronized (proxy) {
                TileEntity possibleChest = getTileEntityFromBlockPos(message.chestPos, serverPlayer.getServerWorld());
                if (possibleChest == null) {
                    LOGGER.info("Can't save, no chest");
                    return;
                }
                StorageConfig conf = possibleChest.getCapability(StorageConfigProvider.STORAGE_CONFIG_CAPABILITY, null);
                if (conf != null) {
                    conf.copyFrom(message.data);
                }else {
                    LOGGER.error("Why doesn't {} have a storageConfig?", possibleChest);
                }
            }
        });
        // No response packet
        return new StorageConfigCreateResponse();
    }
}
