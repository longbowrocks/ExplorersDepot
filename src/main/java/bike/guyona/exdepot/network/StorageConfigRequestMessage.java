package bike.guyona.exdepot.network;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.capability.StorageConfigProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;
import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.getContainerTileEntities;

/**
 * Created by longb on 12/5/2017.
 */
public class StorageConfigRequestMessage implements IMessage, IMessageHandler<StorageConfigRequestMessage, StorageConfigRequestResponse> {
    public StorageConfigRequestMessage(){}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public StorageConfigRequestResponse onMessage(StorageConfigRequestMessage message, MessageContext ctx) {
        // This is the player the packet was sent to the server from
        EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
        //noinspection SynchronizeOnNonFinalField
        synchronized (proxy) {
            List<TileEntity> chests = getContainerTileEntities(serverPlayer.openContainer);
            if (chests.size() > 0) {
                StorageConfig conf = chests.get(0).getCapability(StorageConfigProvider.STORAGE_CONFIG_CAPABILITY, null);
                if (conf != null) {
                    return new StorageConfigRequestResponse(conf);
                }else {
                    LOGGER.error("StorageConfig was never added to {} for some reason.", chests.get(0));
                }
            } else {
                LOGGER.error("StorageConfig requested for {}, which can't have StorageConfig.",
                        serverPlayer.openContainer);
            }
        }
        return new StorageConfigRequestResponse(new StorageConfig());
    }
}
