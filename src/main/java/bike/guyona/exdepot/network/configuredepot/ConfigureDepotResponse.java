package bike.guyona.exdepot.network.configuredepot;

import bike.guyona.exdepot.ExDepotMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
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
                Player player = Minecraft.getInstance().player;
                if (player == null) {
                    ExDepotMod.LOGGER.error("Impossible: got an ingame-only network event while no player was loaded.");
                    return;
                }
                Minecraft.getInstance().player.playSound(obj.configureDepotResult.getSound(), 1,1);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
