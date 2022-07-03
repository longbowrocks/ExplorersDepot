package bike.guyona.exdepot.client.particles;

import bike.guyona.exdepot.particles.DepositingItemParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import org.jetbrains.annotations.Nullable;

public class DepositingItemParticleProvider implements ParticleProvider<DepositingItemParticleOptions> {
    @Nullable
    @Override
    public Particle createParticle(DepositingItemParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        return new DepositingItemParticle(options.stack, level, x, y, z, options.target);
    }
}
