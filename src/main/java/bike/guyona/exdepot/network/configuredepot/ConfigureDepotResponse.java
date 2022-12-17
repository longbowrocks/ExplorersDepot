package bike.guyona.exdepot.network.configuredepot;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ConfigureDepotResponse {
    ConfigureDepotResult configureDepotResult;

    public ConfigureDepotResponse(ConfigureDepotResult configureDepotResult) {
        this.configureDepotResult = configureDepotResult;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(configureDepotResult.ordinal());
    }

    public static ConfigureDepotResponse decode(FriendlyByteBuf buf) {
        return new ConfigureDepotResponse(ConfigureDepotResult.values()[buf.readInt()]);
    }

    public static void handle(ConfigureDepotResponse obj, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                bike.guyona.exdepot.client.network.configuredepot.ConfigureDepotResponse.playConfigureDepotSound(obj.configureDepotResult);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
