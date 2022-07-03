package bike.guyona.exdepot.particles;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.NotNull;


public class DepositingItemParticleType extends ParticleType<DepositingItemParticleOptions> {
    private final Codec<DepositingItemParticleOptions> codec = Codec.unit(DepositingItemParticleOptions::new);

    public DepositingItemParticleType() {
        super(false, new DepositingItemParticleOptions.Provider());
    }

    @Override
    @NotNull
    public Codec<DepositingItemParticleOptions> codec() {
        return this.codec;
    }
}
