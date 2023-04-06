package bike.guyona.exdepot.network.getdepot;

import bike.guyona.exdepot.capabilities.DefaultDepotCapability;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.network.configuredepot.ConfigureDepotResult;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GetDepotResponse {
    ConfigureDepotResult configureDepotResult;
    IDepotCapability depot;
    BlockPos loc;

    public GetDepotResponse(ConfigureDepotResult configureDepotResult, IDepotCapability cap, BlockPos loc) {
        this.configureDepotResult = configureDepotResult;
        this.depot = cap;
        this.loc = loc;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(configureDepotResult.ordinal());
        int numDepots = this.depot == null ? 0 : 1;
        buf.writeInt(numDepots);
        if (numDepots > 0) {
            buf.writeNbt(depot.serializeNBT());
        }
        int numLocs = this.loc == null ? 0 : 1;
        buf.writeInt(numLocs);
        if (numLocs > 0) {
            buf.writeBlockPos(this.loc);
        }
    }

    public static GetDepotResponse decode(FriendlyByteBuf buf) {
        ConfigureDepotResult result = ConfigureDepotResult.values()[buf.readInt()];
        IDepotCapability cap = new DefaultDepotCapability();
        if (buf.readInt() > 0) {
            CompoundTag serializedDepot = buf.readNbt();
            cap.deserializeNBT(serializedDepot);
        }
        BlockPos loc = null;
        if (buf.readInt() > 0) {
            loc = buf.readBlockPos();
        }
        return new GetDepotResponse(result, cap, loc);
    }

    public static void handle(GetDepotResponse obj, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                bike.guyona.exdepot.client.network.configuredepot.ConfigureDepotResponse.playConfigureDepotSound(obj.configureDepotResult);
                if (obj.configureDepotResult == ConfigureDepotResult.SUCCESS && obj.depot != null) {
                    bike.guyona.exdepot.client.network.configuredepotmanual.ConfigureDepotManualResponse.openGui(obj.depot, obj.loc);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}