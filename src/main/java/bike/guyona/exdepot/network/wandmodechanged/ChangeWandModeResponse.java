package bike.guyona.exdepot.network.wandmodechanged;

import bike.guyona.exdepot.items.DepotConfiguratorWandBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChangeWandModeResponse {
    DepotConfiguratorWandBase.Mode newMode;

    public ChangeWandModeResponse(DepotConfiguratorWandBase.Mode newMode) {
        this.newMode = newMode;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(newMode.ordinal());
    }

    public static ChangeWandModeResponse decode(FriendlyByteBuf buf) {
        return new ChangeWandModeResponse(DepotConfiguratorWandBase.Mode.values()[buf.readInt()]);
    }

    public static void handle(ChangeWandModeResponse obj, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                bike.guyona.exdepot.client.network.wandmodechanged.ChangeWandModeResponse.playWandModeSwitchSound();
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
