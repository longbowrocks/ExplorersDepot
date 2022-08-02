package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capabilities.DefaultDepotCapability;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.client.particles.ViewDepotParticle;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class ViewDepotsResponse {
    BlockPos depotLocation;
    String modId;
    boolean simpleDepot;
    int chestFullness;

    public ViewDepotsResponse(BlockPos loc, String modId, boolean simpleDepot, int chestFullness) {
        this.depotLocation = loc;
        this.modId = modId;
        this.simpleDepot = simpleDepot;
        this.chestFullness = chestFullness;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(depotLocation);
        buf.writeUtf(modId == null ? "" : modId);
        buf.writeBoolean(simpleDepot);
        buf.writeInt(chestFullness);
    }

    public static ViewDepotsResponse decode(FriendlyByteBuf buf) {
        BlockPos depotLocation = buf.readBlockPos();
        String modId = buf.readUtf();
        boolean simpleDepot = buf.readBoolean();
        int chestFullness = buf.readInt();
        return new ViewDepotsResponse(depotLocation, modId, simpleDepot, chestFullness);
    }

    public static void handle(ViewDepotsResponse obj, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player == null) {
                    ExDepotMod.LOGGER.error("Impossible: the client doesn't have a player");
                    return;
                }
                ExDepotMod.LOGGER.info("Refreshed cache of {} at {}", obj.modId, obj.depotLocation);
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.particleEngine.add(
                        new ViewDepotParticle(
                                minecraft.level,
                                obj.depotLocation.getX() + 0.5,
                                obj.depotLocation.getY() + 2.0,
                                obj.depotLocation.getZ() + 0.5,
                                obj.modId,
                                obj.simpleDepot,
                                obj.chestFullness
                        )
                );
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
