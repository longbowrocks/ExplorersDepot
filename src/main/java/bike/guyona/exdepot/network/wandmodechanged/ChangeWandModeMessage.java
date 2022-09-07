package bike.guyona.exdepot.network.wandmodechanged;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.items.DepotConfiguratorWandItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

import static bike.guyona.exdepot.ExDepotMod.WAND_ITEM;

public class ChangeWandModeMessage {
    int direction;

    public ChangeWandModeMessage(int direction) {
        this.direction = direction;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(direction);
    }

    public static ChangeWandModeMessage decode(FriendlyByteBuf buf) {
        return new ChangeWandModeMessage(buf.readInt());
    }

    public static void handle(ChangeWandModeMessage obj, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender == null) {
            ExDepotMod.LOGGER.warn("NO ONE sent a ChangeWandModeMessage");
        } else {
            ctx.get().enqueueWork(() -> {
                ItemStack stack = sender.getMainHandItem();
                if (!WAND_ITEM.get().equals(stack.getItem())) {
                    ExDepotMod.LOGGER.error("Impossible: wand was not in main hand, but it must be to send this message.");
                    return;
                }
                DepotConfiguratorWandItem.Mode oldMode = DepotConfiguratorWandItem.getMode(stack);
                DepotConfiguratorWandItem.Mode[] allModes = DepotConfiguratorWandItem.Mode.values();
                DepotConfiguratorWandItem.Mode newMode = allModes[(allModes.length + oldMode.ordinal() + obj.direction) % allModes.length];
                DepotConfiguratorWandItem.setMode(stack, newMode);
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
