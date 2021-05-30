package bike.guyona.exdepot.network;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.proxy.ClientProxy.openConfigurationGui;

public class StorageConfigCreateFromChestResponse {
    private BlockPos chestPos;
    private StorageConfig data;

    public StorageConfigCreateFromChestResponse(){}

    public StorageConfigCreateFromChestResponse(StorageConfig toSend, BlockPos chestPosition) {
        data = toSend;
        chestPos = chestPosition;
    }

    public StorageConfigCreateFromChestResponse(PacketBuffer buf) {
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
        public static void onMessage(StorageConfigCreateFromChestResponse message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if(mc.world != null && mc.player != null) {
                    if(mc.currentScreen instanceof StorageConfigGui) {
                        openConfigurationGui(message.data, message.chestPos);
                    } else {
                        LOGGER.error("Tried to set config from chest, but when I came back the gui was wrong");
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
