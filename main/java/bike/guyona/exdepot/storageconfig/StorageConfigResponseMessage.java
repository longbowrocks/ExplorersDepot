package bike.guyona.exdepot.storageconfig;

import bike.guyona.exdepot.storageconfig.capability.StorageConfig;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by longb on 12/5/2017.
 */
public class StorageConfigResponseMessage implements IMessage {
    private StorageConfig data;

    public StorageConfigResponseMessage(){}

    public StorageConfigResponseMessage(StorageConfig toSend) {
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

    public static class StorageConfigResponseMessageHandler implements IMessageHandler<StorageConfigResponseMessage, IMessage> {
        @Override
        public IMessage onMessage(StorageConfigResponseMessage message, MessageContext ctx) {
            // No response packet
            return null;
        }
    }
}
