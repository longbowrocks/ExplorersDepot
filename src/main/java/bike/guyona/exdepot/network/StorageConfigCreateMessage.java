package bike.guyona.exdepot.network;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.capability.StorageConfigProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.getTileEntityFromBlockPos;

/**
 * Created by longb on 9/9/2017.
 */
public class StorageConfigCreateMessage{
    private BlockPos chestPos;
    private StorageConfig data;

    public StorageConfigCreateMessage(){}

    public StorageConfigCreateMessage(StorageConfig toSend, BlockPos chestPosition) {
        data = toSend;
        chestPos = chestPosition;
    }

    public StorageConfigCreateMessage(PacketBuffer buf) {
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
        public static void onMessage(StorageConfigCreateMessage message, Supplier<NetworkEvent.Context> ctx) {
            // Associate chests with received StorageConfig, and add to cache.
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity serverPlayer = ctx.get().getSender();
                TileEntity possibleChest = getTileEntityFromBlockPos(message.chestPos, serverPlayer.getServerWorld());
                if (possibleChest == null) {
                    LOGGER.info("Can't save, no chest");
                    return;
                }
                StorageConfig conf = possibleChest.getCapability(StorageConfigProvider.STORAGE_CONFIG_CAPABILITY, null).orElse(null);
                if (conf != null) {
                    conf.copyFrom(message.data);
                }else {
                    LOGGER.error("Why doesn't {} have a storageConfig?", possibleChest);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
