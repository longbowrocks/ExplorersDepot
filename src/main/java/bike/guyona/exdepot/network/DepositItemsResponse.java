package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.events.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static bike.guyona.exdepot.events.EventHandler.JUICER;
import static bike.guyona.exdepot.sounds.SoundEvents.DEPOSIT_SOUNDS;

public class DepositItemsResponse {
    public void encode(FriendlyByteBuf buf) {

    }

    public static DepositItemsResponse decode(FriendlyByteBuf buf) {
        return new DepositItemsResponse();
    }

    public static void handle(DepositItemsResponse obj, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player == null) {
                    ExDepotMod.LOGGER.error("Impossible: the client doesn't have a player");
                    return;
                }
                JUICER.addDepositEvent(1);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
