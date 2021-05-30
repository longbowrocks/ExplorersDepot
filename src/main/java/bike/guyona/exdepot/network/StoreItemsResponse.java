package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.proxy.ClientProxy;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.Supplier;

public class StoreItemsResponse {
    protected Map<BlockPos, List<ItemStack>> sortingResults;

    public StoreItemsResponse() {}

    public StoreItemsResponse(Map<BlockPos, List<ItemStack>> toSend) {
        this.sortingResults = toSend;
    }

    public StoreItemsResponse(PacketBuffer buf) {
        this.sortingResults = new HashMap<>();
        PacketBuffer test = new PacketBuffer(buf);
        int sortingResultsCount = test.readInt();
        for (int i=0; i<sortingResultsCount; i++) {
            BlockPos key = test.readBlockPos();
            List<ItemStack> stacks = new Vector<>();
            int stackCount = test.readInt();
            for (int j=0; j<stackCount; j++) {
                stacks.add(test.readItemStack());
            }
            this.sortingResults.put(key, stacks);
        }
    }

    public void encode(PacketBuffer buf) {
        PacketBuffer test = new PacketBuffer(buf);
        test.writeInt(sortingResults.size());
        for (BlockPos blockPos : sortingResults.keySet()) {
            test.writeBlockPos(blockPos);
            List<ItemStack> stacks = sortingResults.get(blockPos);
            test.writeInt(stacks.size());
            for (ItemStack stack : stacks) {
                test.writeItemStack(stack);
            }
        }
    }

    public static class Handler {
        public static void onMessage(StoreItemsResponse message, Supplier<NetworkEvent.Context> ctx) {
            if (ExDepotMod.proxy instanceof ClientProxy) {
                ((ClientProxy)ExDepotMod.proxy).addSortingResults(message.sortingResults);
            } else {
                ExDepotMod.LOGGER.error("Got a StoreItemsResponse on the server side somehow.");
            }
            ctx.get().setPacketHandled(true);
        }
    }
}
