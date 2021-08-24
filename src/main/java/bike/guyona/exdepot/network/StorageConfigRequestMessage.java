package bike.guyona.exdepot.network;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.helpers.GuiHelpers;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.getTileEntityFromBlockPos;

/**
 * Created by longb on 12/5/2017.
 */
public class StorageConfigRequestMessage {
    private BlockPos chestPos;

    public StorageConfigRequestMessage(){ this.chestPos = new BlockPos(-1,-1,-1); }

    public StorageConfigRequestMessage(BlockPos chestPos){ this.chestPos = chestPos; }

    public StorageConfigRequestMessage(PacketBuffer buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        chestPos = new BlockPos(x, y, z);
        LOGGER.info("Received {}", chestPos);
    }

    public void encode(PacketBuffer buf) {
        LOGGER.info("Sending over {}", chestPos);
        buf.writeInt(chestPos.getX());
        buf.writeInt(chestPos.getY());
        buf.writeInt(chestPos.getZ());
    }

    public static class Handler {
        public static void onMessage(StorageConfigRequestMessage message, Supplier<NetworkEvent.Context> ctx) {
            // Associate chests with received StorageConfig, and add to cache.
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity serverPlayer = ctx.get().getSender();
                TileEntity possibleChest = getTileEntityFromBlockPos(message.chestPos, serverPlayer.getServerWorld());
                if (possibleChest == null) {
                    LOGGER.info("No Chest");
                    GuiHelpers.openStorageConfigGui(serverPlayer, message.chestPos, new StorageConfig());
                    return;
                }
                StorageConfig conf = StorageConfig.fromTileEntity(possibleChest);
                if (conf != null) {
                    GuiHelpers.openStorageConfigGui(serverPlayer, message.chestPos, conf);
                    return;
                }
                LOGGER.info("No Config");
                GuiHelpers.openStorageConfigGui(serverPlayer, message.chestPos, new StorageConfig());
            });
            ctx.get().setPacketHandled(true);
        }
    }

}
