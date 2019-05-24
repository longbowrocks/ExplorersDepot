package bike.guyona.exdepot.gui.particle;

import bike.guyona.exdepot.ExDepotMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.vecmath.Matrix4d;

public class ParticleFlyingItem extends Particle {
    protected double startX;
    protected double startY;
    protected double startZ;
    protected double targX;
    protected double targY;
    protected double targZ;
    private static final double TICKS_PER_SECOND = 20;
    // keep the rotation matrices and point so we don't need to free and rebuild them all the time.
    private Matrix4d point;
    private Matrix4d rotAroundZAxis;
    private Matrix4d rotFromZAxis;
    private Matrix4d rotFromXZPlane;

    public ParticleFlyingItem(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xTargIn, double yTargIn, double zTargIn, ItemStack stack)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        this.canCollide = false;
        double desiredFlightTime = 3;
        this.setMaxAge((int)Math.ceil(TICKS_PER_SECOND * desiredFlightTime));

        this.particleGravity = 1;
        this.startX = xCoordIn;
        this.startY = yCoordIn;
        this.startZ = zCoordIn;
        this.targX = xTargIn;
        this.targY = yTargIn;
        this.targZ = zTargIn;

        //Calculate motion, given inputs
        this.motionX = (this.targX - this.posX) / desiredFlightTime;
        this.motionZ = (this.targZ - this.posZ) / desiredFlightTime;
        this.motionY = (this.targY - this.posY) / desiredFlightTime;

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

        // Get the coords we expect to have along the line between start and target.
        double nextPosX = this.startX + this.motionX * this.particleAge / TICKS_PER_SECOND;
        double nextPosY = this.startY + this.motionY * this.particleAge / TICKS_PER_SECOND;
        double nextPosZ = this.startZ + this.motionZ * this.particleAge / TICKS_PER_SECOND;
        // Modify coords to make particle follow a gradually tightening spiral.
        double[] rotatedPoint = getPointRotatedAroundLine(
                this.targX - this.startX,
                this.targY - this.startY,
                this.targZ - this.startZ,
                1.1-((double)this.particleAge/(double)this.particleMaxAge),
                3 * this.particleAge / TICKS_PER_SECOND
        );
        nextPosX += rotatedPoint[0];
        nextPosY += rotatedPoint[1];
        nextPosZ += rotatedPoint[2];
        this.move(
                nextPosX - this.posX,
                nextPosY - this.posY,
                nextPosZ - this.posZ);
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

//        Matrix4d point = new Matrix4d(
//                0,0,0,0,
//                0,0,0,distanceFromAxisToPoint,
//                0,0,0,0,
//                0,0,0,1
//        );
//        Matrix4d rotAroundZAxis = new Matrix4d(
//                Math.cos(rotationRadians),-Math.sin(rotationRadians),0,0,
//                Math.sin(rotationRadians),Math.cos(rotationRadians),0,0,
//                0,0,1,0,
//                0,0,0,1
//        );
//        Matrix4d rotFromZAxis = new Matrix4d(
//                cosThetaToZAxis,0,-sinThetaToZAxis,0,
//                0,1,0,0,
//                sinThetaToZAxis,0,cosThetaToZAxis,0,
//                0,0,0,1
//        );
//        Matrix4d rotFromXZPlane = new Matrix4d(
//                1,0,0,0,
//                0,cosThetaToXZPlane,sinThetaToXZPlane,0,
//                0,-sinThetaToXZPlane,cosThetaToXZPlane,0,
//                0,0,0,1
//        );
        rotAroundZAxis.mul(point);
        rotFromZAxis.mul(rotAroundZAxis);
        rotFromXZPlane.mul(rotFromZAxis);
        return new double[]{rotFromXZPlane.m03, rotFromXZPlane.m13, rotFromXZPlane.m23};
    }
}
