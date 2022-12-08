package bike.guyona.exdepot.network.wandmodechanged;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.items.DepotConfiguratorWandBase;
import bike.guyona.exdepot.sounds.SoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
                LocalPlayer player = Minecraft.getInstance().player;
                if (player == null) {
                    ExDepotMod.LOGGER.error("Impossible: the client doesn't have a player");
                    return;
                }
                Minecraft.getInstance().player.playSound(SoundEvents.WAND_SWITCH.get(), 1, 1);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
