package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.config.ExDepotConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.*;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.capability.StorageConfigProvider.STORAGE_CONFIG_CAPABILITY;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.isTileEntitySupported;

public class EasyViewConfigsRequestMessage implements IMessage, IMessageHandler<EasyViewConfigsRequestMessage, IMessage> {
    private BlockPos playerPos;

    public EasyViewConfigsRequestMessage(){ this.playerPos = new BlockPos(-1,-1,-1); }

    public EasyViewConfigsRequestMessage(BlockPos playerPos){ this.playerPos = playerPos; }

    @Override
    public void toBytes(ByteBuf buf) {
        LOGGER.info("Sending over {}", playerPos);
        buf.writeInt(playerPos.getX());
        buf.writeInt(playerPos.getY());
        buf.writeInt(playerPos.getZ());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        playerPos = new BlockPos(x, y, z);
        LOGGER.info("Received {}", playerPos);
    }

    @Override
    public EasyViewConfigsRequestResponse onMessage(EasyViewConfigsRequestMessage message, MessageContext ctx) {
        // This is the player the packet was sent to the server from
        EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
        serverPlayer.getServerWorld().addScheduledTask(() -> {
            Vector<TileEntity> nearbyChests = getLocalChests(serverPlayer);
            StorageConfig nearestConfig = null;
            BlockPos nearestConfigLocation = null;
            if (nearbyChests.size() > 0) {
                nearestConfig = nearbyChests.get(0).getCapability(STORAGE_CONFIG_CAPABILITY, null);
                nearestConfigLocation = nearbyChests.get(0).getPos();
            }
            ExDepotMod.NETWORK.sendTo(new EasyViewConfigsRequestResponse(nearestConfig, nearestConfigLocation), serverPlayer);
        });
        return null;
    }

    private static Vector<TileEntity> getLocalChests(EntityPlayerMP player){
        Vector<TileEntity> chests = new Vector<>();
        int chunkDist = (ExDepotConfig.storeRange >> 4) + 1;
        LOGGER.info("Storage range is {} blocks, or {} chunks", ExDepotConfig.storeRange, chunkDist);
        for (int chunkX = player.chunkCoordX-chunkDist; chunkX <= player.chunkCoordX+chunkDist; chunkX++) {
            for (int chunkZ = player.chunkCoordZ-chunkDist; chunkZ <= player.chunkCoordZ+chunkDist; chunkZ++) {
                Collection<TileEntity> entities = player.getServerWorld().getChunkFromChunkCoords(chunkX, chunkZ).getTileEntityMap().values();
                for (TileEntity entity:entities) {
                    if (isTileEntitySupported(entity)){
                        BlockPos chestPos = entity.getPos();
                        if (player.getPosition().getDistance(chestPos.getX(), chestPos.getY(), chestPos.getZ()) <
                                ExDepotConfig.storeRange &&
                                entity.getCapability(STORAGE_CONFIG_CAPABILITY, null) != null) {
                            chests.add(entity);
                        }
                    }
                }
            }
        }
        return chests;
    }
}
