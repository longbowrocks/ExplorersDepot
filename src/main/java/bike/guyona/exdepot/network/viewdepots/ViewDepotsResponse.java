package bike.guyona.exdepot.network.viewdepots;

import bike.guyona.exdepot.helpers.ChestFullness;
import com.mojang.math.Vector3d;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ViewDepotsResponse {
    List<ViewDepotSummary> depotSummaries;

    public ViewDepotsResponse(List<ViewDepotSummary> depotSummaries) {
        this.depotSummaries = depotSummaries;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(depotSummaries.size());
        for (ViewDepotSummary depotSummary : depotSummaries) {
            buf.writeDouble(depotSummary.loc().x);
            buf.writeDouble(depotSummary.loc().y);
            buf.writeDouble(depotSummary.loc().z);
            buf.writeUtf(depotSummary.modId());
            buf.writeBoolean(depotSummary.isSimpleDepot());
            buf.writeInt(depotSummary.chestFullness().ordinal());
        }
    }

    public static ViewDepotsResponse decode(FriendlyByteBuf buf) {
        List<ViewDepotSummary> summaries = new ArrayList<>();
        int numSummaries = buf.readInt();
        for (int i=0; i < numSummaries; i++) {
            Vector3d depotLocation = new Vector3d(
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble()
            );
            String modId = buf.readUtf();
            boolean simpleDepot = buf.readBoolean();
            ChestFullness chestFullness = ChestFullness.values()[buf.readInt()];
            summaries.add(new ViewDepotSummary(depotLocation, modId, simpleDepot, chestFullness));
        }
        return new ViewDepotsResponse(summaries);
    }

    public static void handle(ViewDepotsResponse obj, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                bike.guyona.exdepot.client.network.viewdepots.ViewDepotsResponse.updateViewDepotsCache(obj.depotSummaries);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
