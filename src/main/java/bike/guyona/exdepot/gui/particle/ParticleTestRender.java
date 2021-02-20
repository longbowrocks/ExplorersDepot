package bike.guyona.exdepot.gui.particle;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.helpers.GuiHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;

import static bike.guyona.exdepot.ExDepotMod.MOD_BUTTON_TEXTURES;

public class ParticleTestRender extends Particle {
    private static final ResourceLocation PARTICLE_TEXTURES = new ResourceLocation("textures/particle/particles.png");
    private static final ResourceLocation WAND_TEXTURE = new ResourceLocation(Ref.MODID, "textures/items/storage_configuration_wand.png");
    private static final double TICKS_PER_SECOND = 20;
    private static final int IGNORED = 0;
    private static final int TEXTURE_MAP_SIZE = 256;
    private static final double MIDDLE_OF_BLOCK = 0.5;
    private static final double JUST_ABOVE_BLOCK = 1.1;

    public ParticleTestRender(
            World worldIn,
            double xCoordIn, double yCoordIn, double zCoordIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        this.canCollide = false;
        double desiredFlightTime = 300;
        this.setMaxAge((int)Math.ceil(TICKS_PER_SECOND * desiredFlightTime));

        this.particleGravity = 0;
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;

//        this.setParticleTexture(Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(stack.getItem()));
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
    }

    @Override
    public int getFXLayer()
    {
        return 3;
    }

    public void renderParticle(VertexBuffer buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
//        this.basicallyEntityRendererDrawNameplate(
//                0,6,0,
//                0,
//                0,0,
//                false,
//                false
//        );
//        this.basicallyPiracy(buffer);
//        this.perfectNumbers(buffer);
//        this.displayBox(buffer);
//        this.displayOpaquePanelAboveChest(entityIn, buffer, partialTicks);
         this.displayTranslucentPanelAndSwordAboveChest(entityIn, buffer, partialTicks);
    }

    // net.minecraft.client.renderer.EntityRenderer.drawNameplate()
    private void basicallyEntityRendererDrawNameplate(float x, float y, float z, int verticalShift, float viewerYaw, float viewerPitch, boolean isThirdPersonFrontal, boolean isSneaking){
        String str = "dingle-hopper";
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontRendererIn = mc.fontRenderer;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float)(isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        if (!isSneaking)
        {
            GlStateManager.disableDepth();
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        int i = fontRendererIn.getStringWidth(str) / 2;
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos((double)(-i - 1), (double)(-1 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        vertexbuffer.pos((double)(-i - 1), (double)(8 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        vertexbuffer.pos((double)(i + 1), (double)(8 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        vertexbuffer.pos((double)(i + 1), (double)(-1 + verticalShift), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();

        if (!isSneaking)
        {
            fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, 553648127);
            GlStateManager.enableDepth();
        }

        GlStateManager.depthMask(true);
        fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, isSneaking ? 553648127 : -1);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    // https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/modification-development/1427059-adding-custom-texture-to-custom-particle-effect
    private void basicallyPiracy(VertexBuffer vb) {
        Minecraft mc = Minecraft.getMinecraft();

        mc.renderEngine.bindTexture(WAND_TEXTURE);
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        float tex_size = 1;
        vb.pos(0, 6 + tex_size, 0           ).tex(0,0).endVertex(); // upper left
        vb.pos(0, 6 + tex_size, 0 + tex_size).tex(0,1).endVertex(); // upper right
        vb.pos(0, 6,            0           ).tex(1,0).endVertex(); // lower left
        vb.pos(0, 6,            0 + tex_size).tex(1,1).endVertex(); // lower right
        Tessellator parentOfVb = Tessellator.getInstance();
        parentOfVb.draw();
//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, );
//        super.renderParticle(par1Tessellator, par2, par3, par4, par5, par6, par7);


//        float f = partialTicks;
//
//        float vPosX = (float)(prevPosX + (posX - prevPosX) * f - interpPosX); // particle's X position relative to viewport
//        float vPosY = (float)(prevPosY + (posY - prevPosY) * f - interpPosY); // particle's Y position relative to viewport
//        float xPosZ = (float)(prevPosZ + (posZ - prevPosZ) * f - interpPosZ); // particle's Z position relative to viewport
//
//        float f1 = yawXComponent;
//        float f2 = pitchYComponent;
//        float f3 = yawZComponent;
//        float f4 = combinedYZComponent;
//        float f5 = combinedXYComponent;
//
//        tessellator.getBuffer().pos(vPosX - f1 * f10 - f4 * f10, vPosY - f2 * f10, xPosZ - f3 * f10 - f5 * f10).tex(0, 1).endVertex();
//        tessellator.getBuffer().pos(vPosX - f1 * f10 + f4 * f10, vPosY + f2 * f10, xPosZ - f3 * f10 + f5 * f10).tex(1, 1).endVertex();
//        tessellator.getBuffer().pos(vPosX + f1 * f10 + f4 * f10, vPosY + f2 * f10, xPosZ + f3 * f10 + f5 * f10).tex(1, 0).endVertex();
//        tessellator.getBuffer().pos(vPosX + f1 * f10 - f4 * f10, vPosY - f2 * f10, xPosZ + f3 * f10 - f5 * f10).tex(0, 0).endVertex();
    }

    private void perfectNumbers(VertexBuffer vb) {
        // -1.1313711032271385,2.5030437409877777,2.6890333127230406    1,1
        // -1.1247697100043297,2.7202638685703278,2.6714187413454056    1,0
        // -0.9206017628312111,2.7202638685703278,2.7479345370084047    0,0
        // -0.9272031560540199,2.5030437409877777,2.7655491083860397    0,1

        Minecraft mc = Minecraft.getMinecraft();

        mc.renderEngine.bindTexture(WAND_TEXTURE);
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        // Order matters. Holy fuck.
        vb.pos(-0.9206017628312111,2.7202638685703278,2.7479345370084047).tex(0,0).endVertex(); // upper left
        vb.pos(-1.1247697100043297,2.7202638685703278,2.6714187413454056).tex(1,0).endVertex(); // upper right
        vb.pos(-1.1313711032271385,2.5030437409877777,2.6890333127230406).tex(1,1).endVertex(); // lower right
        vb.pos(-0.9272031560540199,2.5030437409877777,2.7655491083860397).tex(0,1).endVertex(); // lower left
        Tessellator parentOfVb = Tessellator.getInstance();
        parentOfVb.draw();
    }


    private void displayBox(VertexBuffer vb) {
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false); // probably only applies to things drawn here, making them all render in draw order? Awesome if true.
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();

        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        // First three colors don't matter because last one overrides all. However, it seems opengl expects
        // 3 pos and 4 color vars for each vertex so it reads the data funky-like if you don't provide colors.
        vb.pos(-1,2,1).color(IGNORED, IGNORED, IGNORED, IGNORED).endVertex();
        vb.pos(1,2,1).color(IGNORED, IGNORED, IGNORED, IGNORED).endVertex();
        vb.pos(1,0,1).color(IGNORED, IGNORED, IGNORED, IGNORED).endVertex();
        vb.pos(-1,0,1).color(0,0,0, 0.5f).endVertex();
        Tessellator parentOfVb = Tessellator.getInstance();
        parentOfVb.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
    }

    private void displayBoxAboveChest(Entity player, VertexBuffer vb, float partialTicks) {
        // Could use Particle.interpPosX/Y/Z instead if we weren't a "lit" particle rendered by renderLitParticles().
        double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
        double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
        double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
        double deltaXFromChest = playerX - posX;
        double deltaZFromChest = playerZ - posZ;
        float yawToChest = (float)(180 / Math.PI * Math.atan2(deltaXFromChest, deltaZFromChest));

        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();

        GlStateManager.pushMatrix();
        // All coords are relative to world 0,0,0, but transforms are relative to player position. Subtract player
        // position to make transforms relative to world 0,0,0.
        GlStateManager.translate(-playerX, -playerY, -playerZ);
        GlStateManager.translate(posX + MIDDLE_OF_BLOCK,posY + JUST_ABOVE_BLOCK, posZ + MIDDLE_OF_BLOCK);
        GlStateManager.rotate(yawToChest,0,1,0); // Does all OpenGL work in degrees or is this function the black sheep of graphics?
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        // First three colors don't matter because last one overrides all. However, it seems opengl expects a full 28
        // bytes (3 pos and 4 color floats) for each vertex so it reads the data funky-like if you don't provide colors.
        vb.pos(-1,2,0).color(IGNORED, IGNORED, IGNORED, IGNORED).endVertex();
        vb.pos(1,2,0).color(IGNORED, IGNORED, IGNORED, IGNORED).endVertex();
        vb.pos(1,0,0).color(IGNORED, IGNORED, IGNORED, IGNORED).endVertex();
        vb.pos(-1,0,0).color(0,0,0, 0.5f).endVertex();
        Tessellator parentOfVb = Tessellator.getInstance();
        parentOfVb.draw();
        GlStateManager.popMatrix();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
    }


    private void displayOpaquePanelAboveChest(Entity player, VertexBuffer vb, float partialTicks) {
        // Could use Particle.interpPosX/Y/Z instead if we weren't a "lit" particle rendered by renderLitParticles().
        double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
        double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
        double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
        double deltaXFromChest = playerX - posX;
        double deltaZFromChest = playerZ - posZ;
        float yawToChest = (float)(180 / Math.PI * Math.atan2(deltaXFromChest, deltaZFromChest));

//        GlStateManager.disableLighting();
//        GlStateManager.depthMask(false);
//        GlStateManager.enableBlend();
//        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//        GlStateManager.disableTexture2D();

        Minecraft mc = Minecraft.getMinecraft();
        ResourceLocation logoPath = new ResourceLocation(Ref.MODID, "textures/particles/tablet_background.png");
        mc.getTextureManager().bindTexture(logoPath);

        GlStateManager.pushMatrix();
        // All coords are relative to world 0,0,0, but transforms are relative to player position. Subtract player
        // position to make transforms relative to world 0,0,0.
        GlStateManager.translate(-playerX, -playerY, -playerZ);
        GlStateManager.translate(posX + MIDDLE_OF_BLOCK,posY + JUST_ABOVE_BLOCK, posZ + MIDDLE_OF_BLOCK);
        GlStateManager.rotate(yawToChest,0,1,0); // Does all OpenGL work in degrees or is this function the black sheep of graphics?
        int textureIdx = 16;
        float textureLeft = 20*(textureIdx%12);
        float textureRight = textureLeft + 16;
        float textureTop = 20*(textureIdx/12);
        float textureBottom = textureTop + 16;
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(-1,2,0).tex(0,0).endVertex();
        vb.pos(1,2,0).tex(1,0).endVertex();
        vb.pos(1,0,0).tex(1,1).endVertex();
        vb.pos(-1,0,0).tex(0,1).endVertex();
        Tessellator parentOfVb = Tessellator.getInstance();
        parentOfVb.draw();
        GlStateManager.popMatrix();

//        GlStateManager.enableTexture2D();
//        GlStateManager.disableBlend();
//        GlStateManager.depthMask(true);
//        GlStateManager.enableLighting();
    }


    private void displayTranslucentPanelAboveChest(Entity player, VertexBuffer vb, float partialTicks) {
        // Could use Particle.interpPosX/Y/Z instead if we weren't a "lit" particle rendered by renderLitParticles().
        double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
        double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
        double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
        double deltaXFromChest = playerX - posX;
        double deltaZFromChest = playerZ - posZ;
        float yawToChest = (float)(180 / Math.PI * Math.atan2(deltaXFromChest, deltaZFromChest));

//        GlStateManager.disableLighting();
//        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//        GlStateManager.disableTexture2D();

        Minecraft mc = Minecraft.getMinecraft();
        ResourceLocation logoPath = new ResourceLocation(Ref.MODID, "textures/particles/tablet_background.png");
        mc.getTextureManager().bindTexture(logoPath);

        GlStateManager.pushMatrix();
        // All coords are relative to world 0,0,0, but transforms are relative to player position. Subtract player
        // position to make transforms relative to world 0,0,0.
        GlStateManager.translate(-playerX, -playerY, -playerZ);
        GlStateManager.translate(posX + MIDDLE_OF_BLOCK,posY + JUST_ABOVE_BLOCK, posZ + MIDDLE_OF_BLOCK);
        GlStateManager.rotate(yawToChest,0,1,0); // Does all OpenGL work in degrees or is this function the black sheep of graphics?
        int textureIdx = 16;
        float textureLeft = 20*(textureIdx%12);
        float textureRight = textureLeft + 16;
        float textureTop = 20*(textureIdx/12);
        float textureBottom = textureTop + 16;
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(-1,2,0).tex(0,0).endVertex();
        vb.pos(1,2,0).tex(1,0).endVertex();
        vb.pos(1,0,0).tex(1,1).endVertex();
        vb.pos(-1,0,0).tex(0,1).endVertex();
        Tessellator parentOfVb = Tessellator.getInstance();
        parentOfVb.draw();
        GlStateManager.popMatrix();

//        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
//        GlStateManager.depthMask(true);
//        GlStateManager.enableLighting();
    }


    private void displayTranslucentPanelAndSwordAboveChest(Entity player, VertexBuffer vb, float partialTicks) {
        // Could use Particle.interpPosX/Y/Z instead if we weren't a "lit" particle rendered by renderLitParticles().
        double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
        double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
        double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
        double deltaXFromChest = playerX - posX;
        double deltaZFromChest = playerZ - posZ;
        float yawToChest = (float)(180 / Math.PI * Math.atan2(deltaXFromChest, deltaZFromChest));

//        GlStateManager.disableLighting();
//        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//        GlStateManager.disableTexture2D();

        Minecraft mc = Minecraft.getMinecraft();
        ResourceLocation logoPath = new ResourceLocation(Ref.MODID, "textures/particles/tablet_background.png");
        mc.getTextureManager().bindTexture(logoPath);

        GlStateManager.pushMatrix();
        // All coords are relative to world 0,0,0, but transforms are relative to player position. Subtract player
        // position to make transforms relative to world 0,0,0.
        GlStateManager.translate(-playerX, -playerY, -playerZ);
        GlStateManager.translate(posX + MIDDLE_OF_BLOCK,posY + JUST_ABOVE_BLOCK, posZ + MIDDLE_OF_BLOCK);
        GlStateManager.rotate(yawToChest,0,1,0); // Does all OpenGL work in degrees or is this function the black sheep of graphics?
        int textureIdx = 16;
        float textureLeft = 20*(textureIdx%12);
        float textureRight = textureLeft + 16;
        float textureTop = 20*(textureIdx/12);
        float textureBottom = textureTop + 16;
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(-1,2,0).tex(0,0).endVertex();
        vb.pos(1,2,0).tex(1,0).endVertex();
        vb.pos(1,0,0).tex(1,1).endVertex();
        vb.pos(-1,0,0).tex(0,1).endVertex();
        Tessellator parentOfVb = Tessellator.getInstance();
        parentOfVb.draw();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        // All coords are relative to world 0,0,0, but transforms are relative to player position. Subtract player
        // position to make transforms relative to world 0,0,0.
        GlStateManager.translate(-playerX, -playerY, -playerZ);
        GlStateManager.translate(posX + MIDDLE_OF_BLOCK + 2,posY + JUST_ABOVE_BLOCK, posZ + MIDDLE_OF_BLOCK);
        GlStateManager.rotate(yawToChest,0,1,0); // Does all OpenGL work in degrees or is this function the black sheep of graphics?
        RenderItem renderer = Minecraft.getMinecraft().getRenderItem();
        Item item = Item.getByNameOrId("diamond_sword");
        ItemStack stack = item.getDefaultInstance();
        IBakedModel bakedmodel = renderer.getItemModelWithOverrides(stack, (World)null, Minecraft.getMinecraft().player);
        bakedmodel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(bakedmodel, ItemCameraTransforms.TransformType.GUI, false);
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE); // Swap back to vanilla texture for now.
        renderer.renderItem(stack, bakedmodel);
        GlStateManager.popMatrix();


//        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
//        GlStateManager.depthMask(true);
//        GlStateManager.enableLighting();
    }
}
