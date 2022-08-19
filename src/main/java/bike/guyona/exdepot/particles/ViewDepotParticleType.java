package bike.guyona.exdepot.particles;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.NotNull;

public class ViewDepotParticleType extends ParticleType<ViewDepotParticleOptions> {
    private final Codec<ViewDepotParticleOptions> codec = Codec.unit(ViewDepotParticleOptions::new);

    public ViewDepotParticleType() {
        super(false, new ViewDepotParticleOptions.Provider());
    }

    @Override
    @NotNull
    public Codec<ViewDepotParticleOptions> codec() {
        return this.codec;
    }
}
