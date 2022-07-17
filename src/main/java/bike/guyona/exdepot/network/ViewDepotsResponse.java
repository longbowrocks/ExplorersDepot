package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capabilities.DefaultDepotCapability;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.client.helpers.GuiHelpers;
import bike.guyona.exdepot.client.particles.ViewDepotParticle;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
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

import static bike.guyona.exdepot.events.EventHandler.JUICER;

public class ViewDepotsResponse {
    IDepotCapability depotCap;
    BlockPos depotLocation;

    public ViewDepotsResponse(IDepotCapability cap, BlockPos loc) {
        depotCap = cap;
        depotLocation = loc;
    }

    public void encode(FriendlyByteBuf buf) {
        int numDepots = depotCap == null ? 0 : 1;
        buf.writeInt(numDepots);
        if (numDepots > 0) {
            buf.writeNbt(depotCap.serializeNBT());
            buf.writeBlockPos(depotLocation);
        }
    }

    public static ViewDepotsResponse decode(FriendlyByteBuf buf) {
        int numDepots = buf.readInt();
        if (numDepots > 0) {
            IDepotCapability depotCap = new DefaultDepotCapability();
            depotCap.deserializeNBT(buf.readNbt());
            BlockPos depotLocation = buf.readBlockPos();
            return new ViewDepotsResponse(depotCap, depotLocation);
        }
        return new ViewDepotsResponse(null, null);
    }

    public static void handle(ViewDepotsResponse obj, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player == null) {
                    ExDepotMod.LOGGER.error("Impossible: the client doesn't have a player");
                    return;
                }
                if (obj.depotLocation == null || obj.depotCap == null) {
                    ExDepotMod.LOGGER.warn("Server responded with an empty ViewDepotsResponse. It shouldn't bother.");
                    return;
                }
                ExDepotMod.LOGGER.info("Refreshed cache of {} at {}", obj.depotCap, obj.depotLocation);
                Set<ModSortingRule> modRules = obj.depotCap.getRules(ModSortingRule.class);
                Optional<String> modId = modRules.stream().map(ModSortingRule::getModId).findFirst();
                if (modId.isEmpty()) {
                    ExDepotMod.LOGGER.warn("Depot contains no mod sorting rules");
                    return;
                }
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.particleEngine.add(
                        new ViewDepotParticle(
                                minecraft.level,
                                obj.depotLocation.getX() + 0.5,
                                obj.depotLocation.getY() + 2.0,
                                obj.depotLocation.getZ() + 0.5,
                                modId.get()
                        )
                );
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
