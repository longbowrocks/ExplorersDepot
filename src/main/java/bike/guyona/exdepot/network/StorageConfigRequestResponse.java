package bike.guyona.exdepot.network;

import bike.guyona.exdepot.capability.StorageConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static bike.guyona.exdepot.proxy.ClientProxy.openConfigurationGui;

/**
 * Created by longb on 12/5/2017.
 */
public class StorageConfigRequestResponse {
    private BlockPos chestPos;
    private StorageConfig data;

    public StorageConfigRequestResponse(){}

    public StorageConfigRequestResponse(StorageConfig toSend, BlockPos chestPosition) {
        data = toSend;
        chestPos = chestPosition;
    }

    public StorageConfigRequestResponse(PacketBuffer buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        chestPos = new BlockPos(x, y, z);

        int objLength = buf.readInt();
        byte[] bytes = new byte[objLength];
        buf.readBytes(bytes);
        data = StorageConfig.fromBytes(bytes);
    }

    public void encode(PacketBuffer buf) {
        buf.writeInt(chestPos.getX());
        buf.writeInt(chestPos.getY());
        buf.writeInt(chestPos.getZ());

        byte[] bytes = data.toBytes();
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public static class Handler {
        public static void onMessage(StorageConfigRequestResponse message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if(mc.world != null && mc.player != null) {
                    if(mc.currentScreen == null) {
                        openConfigurationGui(message.data, message.chestPos);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
