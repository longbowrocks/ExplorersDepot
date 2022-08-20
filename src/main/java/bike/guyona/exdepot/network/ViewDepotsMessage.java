package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.config.ExDepotConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.function.Supplier;

import static bike.guyona.exdepot.ExDepotMod.NETWORK_INSTANCE;
import static bike.guyona.exdepot.capabilities.DepotCapabilityProvider.DEPOT_CAPABILITY;
import static net.minecraft.client.renderer.LevelRenderer.CHUNK_SIZE;

public class ViewDepotsMessage {
    public void encode(FriendlyByteBuf buf) {}

    public static ViewDepotsMessage decode(FriendlyByteBuf buf) {
        return new ViewDepotsMessage();
    }

    public static void handle(ViewDepotsMessage obj, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender == null) {
            ExDepotMod.LOGGER.warn("NO ONE sent a ViewDepotsMessage");
        } else {
            ctx.get().enqueueWork(() -> {
                Vector<BlockEntity> nearbyChests = getLocalChests(sender);
                List<ViewDepotSummary> depotSummaries = new ArrayList<>();
                for (BlockEntity chest : nearbyChests) {
                    chest.getCapability(DEPOT_CAPABILITY, Direction.UP).ifPresent((cap) -> {
                        depotSummaries.add(ViewDepotSummary.fromDepot(chest, cap));
                    });
                }
                NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender), new ViewDepotsResponse(depotSummaries));
            });
        }
        ctx.get().setPacketHandled(true);
    }

    private static Vector<BlockEntity> getLocalChests(ServerPlayer player){
        Vector<BlockEntity> chests = new Vector<>();
        int chunkDist = (ExDepotConfig.storeRange.get() / CHUNK_SIZE) + 1;
        ExDepotMod.LOGGER.debug("Storage range is {} blocks, or {} chunks", ExDepotConfig.storeRange.get(), chunkDist);
        for (int chunkX = player.chunkPosition().x-chunkDist; chunkX <= player.chunkPosition().x+chunkDist; chunkX++) {
            for (int chunkZ = player.chunkPosition().z-chunkDist; chunkZ <= player.chunkPosition().z+chunkDist; chunkZ++) {
                Collection<BlockEntity> entities = player.level.getChunk(chunkX, chunkZ).getBlockEntities().values();
                for (BlockEntity entity:entities) {
                    LazyOptional<IDepotCapability> lazyDepot = entity.getCapability(DEPOT_CAPABILITY, Direction.UP);
                    lazyDepot.ifPresent((depotCap) -> {
                        BlockPos chestPos = entity.getBlockPos();
                        if (player.position().distanceToSqr(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < ExDepotConfig.storeRange.get()*ExDepotConfig.storeRange.get()) {
                            chests.add(entity);
                        }
                    });
                }
            }
        }
        return chests;
    }


}