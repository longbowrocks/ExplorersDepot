package bike.guyona.exdepot.network.wandmodechanged;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.items.DepotConfiguratorWandBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

import static bike.guyona.exdepot.ExDepotMod.AUTO_WAND_ITEM;
import static bike.guyona.exdepot.ExDepotMod.NETWORK_INSTANCE;

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
                if (!DepotConfiguratorWandBase.isWand(stack.getItem())) {
                    ExDepotMod.LOGGER.error("Impossible: wand was not in main hand, but it must be to send this message.");
                    return;
                }
                DepotConfiguratorWandBase.Mode oldMode = DepotConfiguratorWandBase.Mode.getMode((DepotConfiguratorWandBase) stack.getItem());
                DepotConfiguratorWandBase.Mode[] allModes = DepotConfiguratorWandBase.Mode.values();
                DepotConfiguratorWandBase.Mode newMode = allModes[(allModes.length + oldMode.ordinal() + obj.direction) % allModes.length];
                int mainHandIdx = sender.getInventory().selected;
                sender.getInventory().setItem(mainHandIdx, new ItemStack(newMode.getItem()));
                NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender), new ChangeWandModeResponse(newMode));
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
