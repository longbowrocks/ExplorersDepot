package bike.guyona.exdepot.network;

import bike.guyona.exdepot.capability.StorageConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static bike.guyona.exdepot.ExDepotMod.openConfigurationGui;

/**
 * Created by longb on 12/5/2017.
 */
public class StorageConfigRequestResponse implements IMessage, IMessageHandler<StorageConfigRequestResponse, IMessage> {
    private StorageConfig data;

    public StorageConfigRequestResponse(){}

    public StorageConfigRequestResponse(StorageConfig toSend) {
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
    public IMessage onMessage(StorageConfigRequestResponse message, MessageContext ctx) {
        avoidClassNotFound(message, ctx);
        return null;
    }

    @SideOnly(Side.CLIENT)
    private IMessage avoidClassNotFound(StorageConfigRequestResponse message, MessageContext ctx) {
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
