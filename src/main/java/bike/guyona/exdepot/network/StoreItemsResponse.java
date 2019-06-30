package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.proxy.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class StoreItemsResponse implements IMessage, IMessageHandler<StoreItemsResponse, IMessage> {
    protected Map<BlockPos, List<ItemStack>> sortingResults;

    public StoreItemsResponse() {}

    public StoreItemsResponse(Map<BlockPos, List<ItemStack>> toSend) {
        this.sortingResults = toSend;
    }

    @Override
    public void toBytes(ByteBuf buf) {
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

    @Override
    public void fromBytes(ByteBuf buf) {
        this.sortingResults = new HashMap<>();
        PacketBuffer test = new PacketBuffer(buf);
        int sortingResultsCount = test.readInt();
        for (int i=0; i<sortingResultsCount; i++) {
            BlockPos key = test.readBlockPos();
            List<ItemStack> stacks = new Vector<>();
            int stackCount = test.readInt();
            for (int j=0; j<stackCount; j++) {
                try {
                    stacks.add(test.readItemStack());
                } catch (IOException e) {
                    ExDepotMod.LOGGER.error("Couldn't read item stack {} in sorting results for chest at {}, {}, {}",
                            j, key.getX(), key.getY(), key.getZ());
                }
            }
            this.sortingResults.put(key, stacks);
        }
    }

    @Override
    public IMessage onMessage(StoreItemsResponse message, MessageContext ctx) {
        if (ExDepotMod.proxy instanceof ClientProxy) {
            ((ClientProxy)ExDepotMod.proxy).addSortingResults(message.sortingResults);
        } else {
            ExDepotMod.LOGGER.error("Got a StoreItemsResponse on the server side somehow.");
        }
        return null;
    }
}
