package bike.guyona.exdepot.network;

import bike.guyona.exdepot.gui.StorageConfigGui;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.proxy.ClientProxy.openConfigurationGui;

public class StorageConfigCreateResponse {
    public StorageConfigCreateResponse() {}

    public StorageConfigCreateResponse(PacketBuffer buf) {}

    public void encode(PacketBuffer buf) {}

    public static class Handler {
        public static void onMessage(StorageConfigCreateResponse message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if(mc.world != null && mc.player != null) {
                    if(mc.currentScreen instanceof StorageConfigGui) {
                        mc.player.closeScreen();
                    } else {
                        LOGGER.error("createResp screen is "+(mc.currentScreen == null ? "NULL" : mc.currentScreen.toString()));
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }

    }
}
