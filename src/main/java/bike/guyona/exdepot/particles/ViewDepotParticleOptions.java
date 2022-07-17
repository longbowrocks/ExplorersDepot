package bike.guyona.exdepot.particles;

import bike.guyona.exdepot.capabilities.DefaultDepotCapability;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.network.ViewDepotsResponse;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import static bike.guyona.exdepot.ExDepotMod.VIEW_DEPOT_PARTICLE_TYPE;

public class ViewDepotParticleOptions implements ParticleOptions {
    public final IDepotCapability depotCap;
    @NotNull
    public final BlockPos depotLocation;

    public ViewDepotParticleOptions() {
        depotCap = null;
        depotLocation = new BlockPos(0,0,0);
    }

    public ViewDepotParticleOptions(IDepotCapability cap, @NotNull BlockPos loc) {
        depotCap = cap;
        depotLocation = loc;
    }

    @Override
    public ParticleType<?> getType() {
        return VIEW_DEPOT_PARTICLE_TYPE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        int numDepots = depotCap == null ? 0 : 1;
        buf.writeInt(numDepots);
        if (numDepots > 0) {
            buf.writeNbt(depotCap.serializeNBT());
            buf.writeBlockPos(depotLocation);
        }
    }

    @Override
    public String writeToString() {
        return String.format("Displaying depot at: %s", depotLocation.toString());
    }

    // TODO: This class is deprecated. What am I supposed to use instead?
    public static class Provider implements ParticleOptions.Deserializer<ViewDepotParticleOptions> {
        public Provider() {}

        @Override
        @NotNull
        public ViewDepotParticleOptions fromCommand(ParticleType<ViewDepotParticleOptions> type, StringReader buf) throws CommandSyntaxException {
            Message message = new TextComponent("Command not implemented");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
        }

        @Override
        @NotNull
        public ViewDepotParticleOptions fromNetwork(ParticleType<ViewDepotParticleOptions> type, FriendlyByteBuf buf) {

            int numDepots = buf.readInt();
            if (numDepots > 0) {
                IDepotCapability depotCap = new DefaultDepotCapability();
                depotCap.deserializeNBT(buf.readNbt());
                BlockPos depotLocation = buf.readBlockPos();
                return new ViewDepotParticleOptions(depotCap, depotLocation);
            }
            return new ViewDepotParticleOptions();
        }
    }
}
