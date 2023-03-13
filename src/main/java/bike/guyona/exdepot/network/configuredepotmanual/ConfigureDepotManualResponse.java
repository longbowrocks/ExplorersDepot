package bike.guyona.exdepot.network.configuredepotmanual;

import bike.guyona.exdepot.capabilities.DefaultDepotCapability;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.network.configuredepot.ConfigureDepotResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ConfigureDepotManualResponse {
    ConfigureDepotResult configureDepotResult;
    IDepotCapability depot;

    public ConfigureDepotManualResponse(ConfigureDepotResult configureDepotResult, IDepotCapability cap) {
        this.configureDepotResult = configureDepotResult;
        this.depot = cap;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(configureDepotResult.ordinal());
        if (this.depot == null) {
            buf.writeInt(0);
        } else {
            buf.writeNbt(depot.serializeNBT());
        }
    }

    public static ConfigureDepotManualResponse decode(FriendlyByteBuf buf) {
        ConfigureDepotResult result = ConfigureDepotResult.values()[buf.readInt()];
        CompoundTag serializedDepot = buf.readNbt();
        IDepotCapability cap = new DefaultDepotCapability();
        cap.deserializeNBT(serializedDepot);
        return new ConfigureDepotManualResponse(result, cap);
    }

    public static void handle(ConfigureDepotManualResponse obj, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                bike.guyona.exdepot.client.network.configuredepot.ConfigureDepotResponse.playConfigureDepotSound(obj.configureDepotResult);
                if (obj.configureDepotResult == ConfigureDepotResult.SUCCESS && obj.depot != null) {
                    bike.guyona.exdepot.client.network.configuredepotmanual.ConfigureDepotManualResponse.openGui(obj.depot);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}