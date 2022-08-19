package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.events.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static bike.guyona.exdepot.events.EventHandler.JUICER;

public class DepositItemsResponse {
    Map<BlockPos, List<ItemStack>> sortingResults;

    public DepositItemsResponse(Map<BlockPos, List<ItemStack>> toSend) {
        this.sortingResults = toSend;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(sortingResults.size());
        for (BlockPos depotLocation : sortingResults.keySet()) {
            buf.writeBlockPos(depotLocation);
            buf.writeInt(sortingResults.get(depotLocation).size());
            for (ItemStack stack : sortingResults.get(depotLocation)) {
                buf.writeItemStack(stack, false); // TODO: try true with TinkersConstruct. Might work.
            }
        }
    }

    public static DepositItemsResponse decode(FriendlyByteBuf buf) {
        Map<BlockPos, List<ItemStack>> sortingResults = new HashMap<>();
        int numDepotLocations = buf.readInt();
        for (int i=0; i<numDepotLocations; i++) {
            BlockPos depotLocation = buf.readBlockPos();
            sortingResults.put(depotLocation, new ArrayList<>());
            int numStacks = buf.readInt();
            for (int j=0; j<numStacks; j++) {
                sortingResults.get(depotLocation).add(buf.readItem());
            }
        }
        return new DepositItemsResponse(sortingResults);
    }

    public static void handle(DepositItemsResponse obj, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player == null) {
                    ExDepotMod.LOGGER.error("Impossible: the client doesn't have a player");
                    return;
                }
                JUICER.enqueueDepositEvent(obj.sortingResults);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
