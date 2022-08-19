package bike.guyona.exdepot.particles;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.math.Vector3d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import static bike.guyona.exdepot.ExDepotMod.DEPOSITING_ITEM_PARTICLE_TYPE;

public class DepositingItemParticleOptions implements ParticleOptions {
    public final ItemStack stack;
    public final Vector3d source;
    public final BlockPos target;

    public DepositingItemParticleOptions() {
        stack = new ItemStack(Items.CHEST);
        source = new Vector3d(0, 0, 0);
        this.target = new BlockPos(0,0,0);
    }

    public DepositingItemParticleOptions(ItemStack stack, double x, double y, double z, BlockPos target) {
        this.stack = stack;
        this.source = new Vector3d(x, y, z);
        this.target = target;
    }

    @Override
    @NotNull
    public ParticleType<?> getType() {
        return DEPOSITING_ITEM_PARTICLE_TYPE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeItemStack(stack, false);
        buf.writeDouble(source.x);
        buf.writeDouble(source.y);
        buf.writeDouble(source.z);
        buf.writeDouble(target.getX());
        buf.writeDouble(target.getY());
        buf.writeDouble(target.getZ());
    }

    @Override
    @NotNull
    public String writeToString() {
        return String.format("%s started:%s goingto:%s", stack.toString(), source.toString(), target.toString());
    }

    // TODO: This class is deprecated. What am I supposed to use instead?
    public static class Provider implements ParticleOptions.Deserializer<DepositingItemParticleOptions> {
        public Provider() {}

        @Override
        @NotNull
        public DepositingItemParticleOptions fromCommand(ParticleType<DepositingItemParticleOptions> type, StringReader buf) throws CommandSyntaxException {
            Message message = Component.literal("Command not implemented");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
        }

        @Override
        @NotNull
        public DepositingItemParticleOptions fromNetwork(ParticleType<DepositingItemParticleOptions> type, FriendlyByteBuf buf) {
            return new DepositingItemParticleOptions(
                    buf.readItem(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    new BlockPos(buf.readInt(), buf.readInt(), buf.readInt())
            );
        }
    }
}
