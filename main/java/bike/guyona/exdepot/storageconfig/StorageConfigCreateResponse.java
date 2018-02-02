package bike.guyona.exdepot.storageconfig;

import bike.guyona.exdepot.storageconfig.gui.StorageConfigGui;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

public class StorageConfigCreateResponse implements IMessage {
    public StorageConfigCreateResponse() {}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    public static class StorageConfigCreateResponseHandler implements IMessageHandler<StorageConfigCreateResponse, IMessage> {
        @Override
        public IMessage onMessage(StorageConfigCreateResponse message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Minecraft mc = Minecraft.getMinecraft();
                if(mc.world != null && mc.player != null) {
                    if(mc.currentScreen != null && mc.currentScreen instanceof StorageConfigGui) {
                        mc.player.closeScreen();
                    } else {
                        LOGGER.error("createResp screen is "+(mc.currentScreen == null ? "NULL" : mc.currentScreen.toString()));
                    }
                }
            });
            // No response packet
            return null;
        }
    }
}
