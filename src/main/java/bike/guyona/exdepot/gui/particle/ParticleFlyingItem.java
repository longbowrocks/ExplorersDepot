package bike.guyona.exdepot.gui.particle;

import bike.guyona.exdepot.ExDepotMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ParticleFlyingItem extends Particle {
    protected double targX;
    protected double targY;
    protected double targZ;
    // so we can have the item follow a ballistic trajectory, but only move slowly at the start so we can see it clearly
    protected double timeDilationFactor;
    private static final double TIME_TO_MAX_SPEED = 1;
    private static final double TICKS_PER_SECOND = 20;

    public ParticleFlyingItem(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xTargIn, double yTargIn, double zTargIn, ItemStack stack)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        this.canCollide = false;
        double desiredFlightTime = 1;
        double unDilatedFlightTime = desiredFlightTime - TIME_TO_MAX_SPEED / 2;
        this.setMaxAge((int)Math.ceil(TICKS_PER_SECOND * desiredFlightTime));

        this.particleGravity = 1;
        this.timeDilationFactor = 0;
        this.targX = xTargIn;
        this.targY = yTargIn;
        this.targZ = zTargIn;

        //Calculate motion, given inputs
        this.motionX = (this.targX - this.posX) / unDilatedFlightTime;
        this.motionZ = (this.targZ - this.posZ) / unDilatedFlightTime;
        // Kinematics. v0 = (dx - .5a * dt^2) / dt
        this.motionY = ((this.targY - this.posY) - 0.5 * -this.particleGravity * Math.pow(unDilatedFlightTime, 2)) / unDilatedFlightTime;

        this.setParticleTexture(Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(stack.getItem()));
    }

    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setExpired();
        }
        if (this.timeDilationFactor < 1) {
            this.timeDilationFactor += 1.0 / (TICKS_PER_SECOND * TIME_TO_MAX_SPEED);
        }

        this.motionY -= this.particleGravity / TICKS_PER_SECOND * this.timeDilationFactor;
        this.move(
                this.motionX / TICKS_PER_SECOND * this.timeDilationFactor,
                this.motionY / TICKS_PER_SECOND * this.timeDilationFactor,
                this.motionZ / TICKS_PER_SECOND * this.timeDilationFactor);
    }

    @Override
    public int getFXLayer()
    {
        return 1;
    }
}
