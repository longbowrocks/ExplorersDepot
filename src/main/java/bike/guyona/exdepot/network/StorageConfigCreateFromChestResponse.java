package bike.guyona.exdepot.network;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.openConfigurationGui;

public class StorageConfigCreateFromChestResponse implements IMessage, IMessageHandler<StorageConfigCreateFromChestResponse, IMessage> {
    private StorageConfig data;

    public StorageConfigCreateFromChestResponse(){}

    public StorageConfigCreateFromChestResponse(StorageConfig toSend) {
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
    public IMessage onMessage(StorageConfigCreateFromChestResponse message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Minecraft mc = Minecraft.getMinecraft();
            if(mc.world != null && mc.player != null) {
                if(mc.currentScreen != null && mc.currentScreen instanceof StorageConfigGui) {
                    openConfigurationGui(message.data);
                } else {
                    LOGGER.error("Tried to set config from chest, but when I came back the gui was wrong");
                }
            }
        });
        // No response packet
        return null;
    }
}
