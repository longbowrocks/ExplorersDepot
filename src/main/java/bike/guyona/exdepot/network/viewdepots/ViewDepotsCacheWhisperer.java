package bike.guyona.exdepot.network.viewdepots;

import bike.guyona.exdepot.config.ExDepotConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.NETWORK_INSTANCE;
import static bike.guyona.exdepot.capabilities.DepotCapabilityProvider.DEPOT_CAPABILITY;

// The vast majority of this class is in the client code.
public class ViewDepotsCacheWhisperer {
    // Server can send an immediate update if things change.
    public static void triggerUpdateFromServer(Level depotLevel, BlockPos depotPos) {
        depotLevel.players()
                .stream()
                .filter(player -> player.distanceToSqr(depotPos.getX(), depotPos.getY(), depotPos.getZ()) < Math.pow(ExDepotConfig.storeRange.get(), 2))
                .map(player -> (ServerPlayer)player)
                .forEach(player -> {
                    Vector<BlockEntity> nearbyChests = ViewDepotsMessage.getLocalChests(depotLevel, player.position());
                    List<ViewDepotSummary> depotSummariesNearPlayer = new ArrayList<>();
                    for (BlockEntity chest : nearbyChests) {
                        chest.getCapability(DEPOT_CAPABILITY, Direction.UP).ifPresent((cap) -> {
                            depotSummariesNearPlayer.add(ViewDepotSummary.fromDepot(chest, cap));
                        });
                    }
                    NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ViewDepotsResponse(depotSummariesNearPlayer));
                });
    }
}
