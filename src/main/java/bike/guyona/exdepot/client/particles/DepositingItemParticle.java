package bike.guyona.exdepot.client.particles;

import bike.guyona.exdepot.ExDepotMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;


//Largely copied from net.minecraft.client.particle.ItemPickupParticle
@OnlyIn(Dist.CLIENT)
public class DepositingItemParticle extends Particle {
    private final RenderBuffers renderBuffers;
    private final Entity itemEntity;
    private final BlockPos target;
    private final double startX;
    private final double startY;
    private final double startZ;
    private final EntityRenderDispatcher entityRenderDispatcher;

    public DepositingItemParticle(ItemStack stack, ClientLevel level, double x, double y, double z, BlockPos target) {
        super(level, x, y, z);
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.target = target;
        ItemEntity item = new ItemEntity(level, x, y, z, stack); // TODO: does this expire, or live in world?
        this.itemEntity = this.getSafeCopy(item);
        this.entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        this.renderBuffers = Minecraft.getInstance().renderBuffers();

        this.lifetime = 50;
        this.gravity = 0.0F;
    }

    private Entity getSafeCopy(Entity p_107037_) {
        return (Entity)(!(p_107037_ instanceof ItemEntity) ? p_107037_ : ((ItemEntity)p_107037_).copy());
    }

    @Override
    public void render(@NotNull VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
        // Keep in mind this isn't tick() so we don't update position. However, culling calculations are based on particle position.
        Vector3d curPos = calculatePosition(partialTicks);
        MultiBufferSource.BufferSource bufferSource = this.renderBuffers.bufferSource();
        Vec3 vec3 = camera.getPosition();
        this.entityRenderDispatcher.render(
                this.itemEntity,
                curPos.x - vec3.x(),
                curPos.y - vec3.y(),
                curPos.z - vec3.z(),
                this.itemEntity.getYRot(),
                partialTicks,
                new PoseStack(),
                bufferSource,
                this.entityRenderDispatcher.getPackedLightCoords(this.itemEntity, partialTicks)
        );
        bufferSource.endBatch();
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }


    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        Vector3d curPos = calculatePosition(0);
        setPos(curPos.x, curPos.y, curPos.z);
    }


    public Vector3d calculatePosition(float partialTicks) {
        if (this.lifetime == 0) { // safety
            return new Vector3d(0,0,0);
        }
        double pctToDest = ((float)this.age + partialTicks) / this.lifetime;
        pctToDest = Math.pow(pctToDest, 2d/3d);
        return new Vector3d(
                Mth.lerp(pctToDest, startX, this.target.getX() + 0.5),
                Mth.lerp(pctToDest, startY, this.target.getY() - 0.5) + getBounceHeight(this.age + partialTicks),
                Mth.lerp(pctToDest, startZ, this.target.getZ() + 0.5)
        );
    }

    public double getBounceHeight(double ageInTicks) {
        int ticksPerSecond = 20;
        double ageInSeconds = ageInTicks / ticksPerSecond;
        double v0 = 4.0;
        double g = -15;
        double bouncyFactor = 0.9;
        double timeToYMax = v0 / -g;
        double bounces = getGeometricIterations(bouncyFactor, ageInSeconds / (2 * timeToYMax));
        if (bounces != bounces) { // NaN
            return 0;
        }

        double lastBounceFactor = Math.pow(bouncyFactor, (int)bounces);
        double finalV0 = v0 * lastBounceFactor;
        double finalTimeToYMax = timeToYMax * lastBounceFactor;
        double yMaxAfterBounces = finalV0 * finalTimeToYMax + 0.5 * g * Math.pow(finalTimeToYMax, 2);
        double speedupFactor = Math.PI / (2 * timeToYMax) / lastBounceFactor;
        double t0 = 2 * timeToYMax * getGeometricSum(bouncyFactor, (int)bounces);
        double dT = ageInSeconds - t0;
        return Math.abs(Math.sin(dT * speedupFactor) * yMaxAfterBounces);
    }

    private double getGeometricSum(double ratio, double iterations) {
        return (1 - Math.pow(ratio, iterations)) / (1 - ratio);
    }

    /**
     * A geometric series is a sum of terms r^0+r^1+...+r^n = Sum
     * This function determines n when given r (aka ratio) and sum.
     *
     * @param ratio r term in a geometric series.
     * @param sum
     * @return number of terms required for a geometric series to reach sum.
     *         NaN if no number of iterations could reach sum.
     */
    private double getGeometricIterations(double ratio, double sum) {
        return Math.log(1 - sum * (1 - ratio)) / Math.log(ratio);
    }
}
