package bike.guyona.exdepot.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


// Some from https://github.com/longbowrocks/ExplorersDepot/compare/master...view_config_without_opening_it#diff-6bcbfc776a0b95590c9f26a81d340db82563cdfdf573b37e4a47e38e6d2b0b77R203
@OnlyIn(Dist.CLIENT)
public class ViewDepotParticle extends Particle {
    private final RenderBuffers renderBuffers;
    private final BlockPos depotBlock;
    private final EntityRenderDispatcher entityRenderDispatcher;

    public ViewDepotParticle(ClientLevel level, double x, double y, double z, BlockPos depotBlock) {
        super(level, x, y, z);
        this.depotBlock = depotBlock;
        this.entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        this.renderBuffers = Minecraft.getInstance().renderBuffers();

        this.lifetime = 50;
        this.gravity = 0.0F;
    }

    @Override
    public void render(VertexConsumer p_107261_, Camera p_107262_, float p_107263_) {
        // TODO
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }
}
