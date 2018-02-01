package bike.guyona.exdepot.storageconfig;

import bike.guyona.exdepot.storageconfig.capability.StorageConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static bike.guyona.exdepot.ExDepotMod.openConfigurationGui;

public class StorageConfigCreateFromChestResponse implements IMessage {
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

    public static class StorageConfigCreateFromChestResponseHandler implements IMessageHandler<StorageConfigCreateFromChestResponse, IMessage> {
        @Override
        public IMessage onMessage(StorageConfigCreateFromChestResponse message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft mc = Minecraft.getMinecraft();
                if(mc.world != null && mc.player != null) {
                    if(mc.currentScreen != null && mc.currentScreen instanceof GuiChest) {
                        openConfigurationGui(message.data);
                    }
                }
            });
            // No response packet
            return null;
        }
    }
}
