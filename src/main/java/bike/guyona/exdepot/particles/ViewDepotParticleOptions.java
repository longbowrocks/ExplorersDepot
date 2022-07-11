package bike.guyona.exdepot.particles;

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
    public final BlockPos depotBlock;

    public ViewDepotParticleOptions() {
        depotBlock = new BlockPos(0,0,0);
    }

    public ViewDepotParticleOptions(BlockPos depotBlock) {
        this.depotBlock = depotBlock;
    }

    @Override
    public ParticleType<?> getType() {
        return VIEW_DEPOT_PARTICLE_TYPE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeInt(depotBlock.getX());
        buf.writeInt(depotBlock.getY());
        buf.writeInt(depotBlock.getZ());
    }

    @Override
    public String writeToString() {
        return String.format("Displaying depot at: %s", depotBlock.toString());
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
            return new ViewDepotParticleOptions(new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()));
        }
    }
}
