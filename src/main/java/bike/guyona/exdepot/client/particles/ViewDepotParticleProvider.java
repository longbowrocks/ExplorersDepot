package bike.guyona.exdepot.client.particles;

import bike.guyona.exdepot.particles.ViewDepotParticleOptions;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ViewDepotParticleProvider implements ParticleProvider<ViewDepotParticleOptions> {
    @Nullable
    @Override
    public Particle createParticle(ViewDepotParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        return new ViewDepotParticle(level, x, y, z, options.modId, options.simpleDepot, options.chestFullness);
    }
}
