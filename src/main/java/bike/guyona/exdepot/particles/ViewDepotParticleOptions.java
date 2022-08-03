package bike.guyona.exdepot.particles;

import bike.guyona.exdepot.helpers.ChestFullness;
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
    @NotNull
    public final BlockPos depotLocation;
    public final String modId;
    public final boolean simpleDepot;
    public final ChestFullness chestFullness;

    public ViewDepotParticleOptions() {
        depotLocation = new BlockPos(0,0,0);
        modId = null;
        simpleDepot = false;
        chestFullness = ChestFullness.EMPTY;
    }

    public ViewDepotParticleOptions(@NotNull BlockPos loc, String modId, boolean simpleDepot, ChestFullness chestFullness) {
        this.depotLocation = loc;
        this.modId = modId;
        this.simpleDepot = simpleDepot;
        this.chestFullness = chestFullness;
    }

    @Override
    public ParticleType<?> getType() {
        return VIEW_DEPOT_PARTICLE_TYPE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeBlockPos(depotLocation);
        buf.writeUtf(modId == null ? "" : modId);
        buf.writeBoolean(simpleDepot);
        buf.writeInt(chestFullness.ordinal());
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
            BlockPos depotLocation = buf.readBlockPos();
            String modId = buf.readUtf();
            boolean simpleDepot = buf.readBoolean();
            ChestFullness chestFullness = ChestFullness.values()[buf.readInt()];
            return new ViewDepotParticleOptions(depotLocation, modId, simpleDepot, chestFullness);
        }
    }
}
