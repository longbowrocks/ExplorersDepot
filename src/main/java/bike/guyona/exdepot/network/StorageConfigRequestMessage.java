package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.capability.StorageConfigProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.getTileEntityFromBlockPos;

/**
 * Created by longb on 12/5/2017.
 */
public class StorageConfigRequestMessage implements IMessage, IMessageHandler<StorageConfigRequestMessage, StorageConfigRequestResponse> {
    private BlockPos chestPos;

    public StorageConfigRequestMessage(){ this.chestPos = new BlockPos(-1,-1,-1); }

    public StorageConfigRequestMessage(BlockPos chestPos){ this.chestPos = chestPos; }

    @Override
    public void toBytes(ByteBuf buf) {
        LOGGER.info("Sending over {}", chestPos);
        buf.writeInt(chestPos.getX());
        buf.writeInt(chestPos.getY());
        buf.writeInt(chestPos.getZ());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        chestPos = new BlockPos(x, y, z);
        LOGGER.info("Received {}", chestPos);
    }

    @Override
    public StorageConfigRequestResponse onMessage(StorageConfigRequestMessage message, MessageContext ctx) {
        // This is the player the packet was sent to the server from
        EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
        serverPlayer.getServerWorld().addScheduledTask(() -> {
            //noinspection SynchronizeOnNonFinalField
            synchronized (proxy) {
                TileEntity possibleChest = getTileEntityFromBlockPos(message.chestPos, serverPlayer.getServerWorld());
                if (possibleChest == null) {
                    ExDepotMod.NETWORK.sendTo(new StorageConfigRequestResponse(new StorageConfig(), message.chestPos), serverPlayer);
                    return;
                }
                StorageConfig conf = StorageConfig.fromTileEntity(possibleChest);
                if (conf != null) {
                    ExDepotMod.NETWORK.sendTo(new StorageConfigRequestResponse(conf, message.chestPos), serverPlayer);
                    return;
                }
                ExDepotMod.NETWORK.sendTo(new StorageConfigRequestResponse(new StorageConfig(), message.chestPos), serverPlayer);
            }
        });
        return null;
    }
}
