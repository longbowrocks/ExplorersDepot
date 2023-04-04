package bike.guyona.exdepot.network.configuredepotmanual;

import bike.guyona.exdepot.capabilities.DefaultDepotCapability;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.helpers.ModSupportHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static bike.guyona.exdepot.capabilities.DepotCapabilityProvider.DEPOT_CAPABILITY;

public class ConfigureDepotManualMessage {
    IDepotCapability depot;
    BlockPos loc;
    public ConfigureDepotManualMessage(IDepotCapability cap, BlockPos loc) {
        this.depot = cap;
        this.loc = loc;
    }
    public void encode(FriendlyByteBuf buf) {
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

    public static ConfigureDepotManualMessage decode(FriendlyByteBuf buf) {
        IDepotCapability cap = new DefaultDepotCapability();
        if (buf.readInt() > 0) {
            CompoundTag serializedDepot = buf.readNbt();
            cap.deserializeNBT(serializedDepot);
        }
        BlockPos loc = null;
        if (buf.readInt() > 0) {
            loc = buf.readBlockPos();
        }
        return new ConfigureDepotManualMessage(cap, loc);
    }

    public static void handle(ConfigureDepotManualMessage obj, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (sender == null || obj.loc == null) {
                return;
            }
            BlockEntity target = sender.level.getBlockEntity(obj.loc);
            if (target == null) {
                return;
            }
            for (BlockEntity e : ModSupportHelpers.getBigDepot(target)) {
                e.getCapability(DEPOT_CAPABILITY, Direction.UP).ifPresent(cap -> cap.copyFrom(obj.depot));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
