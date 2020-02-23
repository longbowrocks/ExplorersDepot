package bike.guyona.exdepot.gui.particle;

import bike.guyona.exdepot.ExDepotMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.vecmath.Matrix4d;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

public class ParticleFlyingItem extends Particle {
    protected double startX;
    protected double startY;
    protected double startZ;
    protected double kinematicX;
    protected double kinematicY;
    protected double kinematicZ;
    protected double accelerationX;
    protected double accelerationY;
    protected double accelerationZ;
    protected double targX;
    protected double targY;
    protected double targZ;
    private static final double TICKS_PER_SECOND = 20;
    // keep the rotation matrices and point so we don't need to free and rebuild them all the time.
    private Matrix4d point;
    private Matrix4d rotAroundZAxis;
    private Matrix4d rotFromZAxis;
    private Matrix4d rotFromXZPlane;

    public ParticleFlyingItem(
            World worldIn,
            double xCoordIn, double yCoordIn, double zCoordIn,
            Vec3d playerLook,
            double xTargIn, double yTargIn, double zTargIn,
            ItemStack stack)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        this.canCollide = false;
        double desiredFlightTime = 3;
        this.setMaxAge((int)Math.ceil(TICKS_PER_SECOND * desiredFlightTime));

        this.particleGravity = 0;
        this.startX = xCoordIn;
        this.startY = yCoordIn;
        this.startZ = zCoordIn;
        this.kinematicX = xCoordIn;
        this.kinematicY = yCoordIn;
        this.kinematicZ = zCoordIn;
        this.targX = xTargIn;
        this.targY = yTargIn;
        this.targZ = zTargIn;

        //Calculate motion, given inputs
        double lookVelScale = 8; // Player sprints at 5.6m/s, so make sure items get in front of sprinter.
        this.motionX = playerLook.xCoord * lookVelScale;
        this.motionY = playerLook.yCoord * lookVelScale;
        this.motionZ = playerLook.zCoord * lookVelScale;

        // Resulting motion should start seemingly going out of player at visible rate, and then accelerate towards the target.
        // dx = vi * dt + a * dt^2 / 2
        // dx - vi * dt  = a * dt^2 / 2
        // 2 * dx -  2 * vi * dt  = a * dt^2
        // (2 * dx -  2 * vi * dt) / dt^2  = a
        double dX = this.targX - this.startX;
        double dY = this.targY - this.startY;
        double dZ = this.targZ - this.startZ;
        double dt = desiredFlightTime;
        this.accelerationX = 2 * (dX - this.motionX * dt) / (dt * dt);
        this.accelerationY = 2 * (dY - this.motionY * dt) / (dt * dt);
        this.accelerationZ = 2 * (dZ - this.motionZ * dt) / (dt * dt);

        this.setParticleTexture(Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(stack.getItem()));

        //Initialize caches
        this.point = new Matrix4d(
                0,0,0,0,
                0,0,0,-1,
                0,0,0,0,
                0,0,0,1
        );
        this.rotAroundZAxis = new Matrix4d(
                -1,-1,0,0,
                -1,-1,0,0,
                0,0,1,0,
                0,0,0,1
        );
        this.rotFromZAxis = new Matrix4d(
                -1,0,-1,0,
                0,1,0,0,
                -1,0,-1,0,
                0,0,0,1
        );
        this.rotFromXZPlane = new Matrix4d(
                1,0,0,0,
                0,-1,-1,0,
                0,-1,-1,0,
                0,0,0,1
        );
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
        double dt = 1 / TICKS_PER_SECOND;

        // Get velocity change based on how far we are through flight.
        double velChangeX = this.accelerationX * dt;
        double velChangeY = this.accelerationY * dt;
        double velChangeZ = this.accelerationZ * dt;

        // Get the coords we expect to have along the line between start and target.
        this.kinematicX += (this.motionX + velChangeX / 2) * dt;
        this.kinematicY += (this.motionY + velChangeY / 2) * dt;
        this.kinematicZ += (this.motionZ + velChangeZ / 2) * dt;

        // Average velocity across dt was applied to position, but update velocity with full velChange.
        this.motionX += velChangeX;
        this.motionY += velChangeY;
        this.motionZ += velChangeZ;

        // Modify coords to make particle follow a gradually tightening spiral.
        double[] rotatedPoint = getPointRotatedAroundLine(
                this.targX - this.startX,
                this.targY - this.startY,
                this.targZ - this.startZ,
                1.1-((double)this.particleAge/(double)this.particleMaxAge),
                2 * this.particleAge / TICKS_PER_SECOND + Math.pow(this.particleAge / TICKS_PER_SECOND, 2.0)
        );
        this.move(
                this.kinematicX + rotatedPoint[0] - this.posX,
                this.kinematicY + rotatedPoint[1] - this.posY,
                this.kinematicZ + rotatedPoint[2] - this.posZ);
    }

    @Override
    public int getFXLayer()
    {
        return 1;
    }

    private double[] getPointRotatedAroundLine(double axisOfRotationX, double axisOfRotationY, double axisOfRotationZ,
                                               double distanceFromAxisToPoint, double rotationRadians){
        double axisLength = Math.sqrt(axisOfRotationX*axisOfRotationX + axisOfRotationY*axisOfRotationY + axisOfRotationZ*axisOfRotationZ);
        double xyProjLength = Math.sqrt(axisOfRotationY*axisOfRotationY + axisOfRotationZ*axisOfRotationZ);
        double sinThetaToXZPlane = axisOfRotationY / xyProjLength;
        double cosThetaToXZPlane = axisOfRotationZ / xyProjLength;
        double sinThetaToZAxis = -axisOfRotationX / axisLength;
        double cosThetaToZAxis = xyProjLength / axisLength;

        point.m03 = 0;
        point.m13 = distanceFromAxisToPoint;
        point.m23 = 0;

        rotAroundZAxis.m00 = Math.cos(rotationRadians);
        rotAroundZAxis.m01 = -Math.sin(rotationRadians);
        rotAroundZAxis.m10 = Math.sin(rotationRadians);
        rotAroundZAxis.m11 = Math.cos(rotationRadians);

        rotFromZAxis.m00 = cosThetaToZAxis;
        rotFromZAxis.m02 = -sinThetaToZAxis;
        rotFromZAxis.m20 = sinThetaToZAxis;
        rotFromZAxis.m22 = cosThetaToZAxis;

        rotFromXZPlane.m11 = cosThetaToXZPlane;
        rotFromXZPlane.m12 = sinThetaToXZPlane;
        rotFromXZPlane.m21 = -sinThetaToXZPlane;
        rotFromXZPlane.m22 = cosThetaToXZPlane;

        //Finally, reset last column and ones because they were changed by previous multiplications.
        rotAroundZAxis.m22 = 1;
        rotAroundZAxis.m03 = 0;
        rotAroundZAxis.m13 = 0;
        rotAroundZAxis.m23 = 0;
        rotFromZAxis.m11 = 1;
        rotFromZAxis.m03 = 0;
        rotFromZAxis.m13 = 0;
        rotFromZAxis.m23 = 0;
        rotFromXZPlane.m00 = 1;
        rotFromXZPlane.m03 = 0;
        rotFromXZPlane.m13 = 0;
        rotFromXZPlane.m23 = 0;

        rotAroundZAxis.mul(point);
        rotFromZAxis.mul(rotAroundZAxis);
        rotFromXZPlane.mul(rotFromZAxis);
        return new double[]{rotFromXZPlane.m03, rotFromXZPlane.m13, rotFromXZPlane.m23};
    }
}
