package bike.guyona.exdepot.gui.ezview;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.helpers.GuiHelpers;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;

public class EasyViewConfigTablet {
    // If you have the wand, these are re-initialized every few seconds. Might be better to do that differently,
    // but few seconds is fine for now.
    private StorageConfig storageConfig;
    private BlockPos storageConfigLocation;

    private float prevRotationYaw;
    private float prevRotationPitch;
    private float prevRotationRoll;
    private float rotationYaw;
    private float rotationPitch;
    private float rotationRoll;
    private float arrowShake;

    // if this gets a StorageConfig, it persists that StorageConfig. It re-renders every frame I guess.
    public EasyViewConfigTablet(StorageConfig storageConfig, BlockPos storageConfigLocation) {
        this.storageConfig = storageConfig;
        this.storageConfigLocation = storageConfigLocation;
    }

    public void render() {
//        System.out.println("Yeah that's the deal.");
//        GlStateManager.disableLighting();

//        GlStateManager.translate(storageConfigLocation.getX(), storageConfigLocation.getY(), storageConfigLocation.getZ());

        // FUUUUUUCCCCCK THIS ONLY RENDERS IN F3 MODE HOW?!
//        Minecraft mc = Minecraft.getMinecraft();
//        double doubleX = mc.player.posX - 0.5;
//        double doubleY = mc.player.posY + 0.1;
//        double doubleZ = mc.player.posZ - 0.5;
//
//        GL11.glPushMatrix();
//        GL11.glTranslated(-doubleX, -doubleY, -doubleZ);
//        GL11.glColor3ub((byte)255,(byte)0,(byte)0);
//        float mx = 9;
//        float my = 9;
//        float mz = 9;
//        GL11.glBegin(GL11.GL_LINES);
//        GL11.glVertex3f(mx+0.4f,my,mz+0.4f);
//        GL11.glVertex3f(mx-0.4f,my,mz-0.4f);
//        GL11.glVertex3f(mx+0.4f,my,mz-0.4f);
//        GL11.glVertex3f(mx-0.4f,my,mz+0.4f);
//        GL11.glEnd();
//        GL11.glPopMatrix();


//        ResourceLocation logoPath = new ResourceLocation(Ref.MODID, "textures/items/storage_configuration_wand.png");
//        BufferedImage logo = GuiHelpers.getModLogo(Loader.instance().getMinecraftModContainer());
//
//        Tessellator tess = Tessellator.getInstance();
//        VertexBuffer wr = tess.getBuffer();
//        Minecraft mc = Minecraft.getMinecraft();
//        mc.renderEngine.bindTexture(logoPath);
//        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
//        wr.pos(0,5+1,0).tex(0,1).endVertex();
//        wr.pos(1,5+1,0).tex(1,1).endVertex();
//        wr.pos(1,5,0).tex(1,0).endVertex();
//        wr.pos(0,5,0).tex(0,0).endVertex();
//        tess.draw();

//        Minecraft mc = Minecraft.getMinecraft();
//        TextureManager tm = mc.getTextureManager();
//        ResourceLocation logoPath = new ResourceLocation(Ref.MODID, "textures.items.storage_configuration_wand");
//        Dimension logoDims = new Dimension(100, 100);
//
//        GlStateManager.enableBlend();
//        mc.renderEngine.bindTexture(logoPath);
//        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
//        wr.pos(offset,                  top + logoDims.height, zLevel).tex(0, 1).endVertex();
//        wr.pos(offset + logoDims.width, top + logoDims.height, zLevel).tex(1, 1).endVertex();
//        wr.pos(offset + logoDims.width, top,                   zLevel).tex(1, 0).endVertex();
//        wr.pos(offset,                  top,                   zLevel).tex(0, 0).endVertex();

//        wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
//        wr.pos(offset, top, zLevel).color(255, 0, 0, 255).endVertex();
//        wr.pos(offset, top + 50, zLevel).color(255, 0, 0, 255).endVertex();
//        GlStateManager.disableBlend();

//        GlStateManager.disableRescaleNormal();
//        GlStateManager.disableBlend();
//        GlStateManager.enableLighting();
        this.basicallyEntityRendererDrawNameplate(
                0,6,0,
                0,
                0,0,
                false,
                false
        );
    }

    private void bindEntityTexture(EasyViewConfigTablet entity) {
        Minecraft mc = Minecraft.getMinecraft();
        ResourceLocation logoPath = new ResourceLocation(Ref.MODID, "textures/items/storage_configuration_wand.png");
        TextureManager tm = mc.getTextureManager();
        tm.bindTexture(logoPath);
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

    // net.minecraft.client.renderer.entity.RenderArrow.doRender()
    private void basicallyRenderArrow() {
        EasyViewConfigTablet entity = this;
        float partialTicks = 0;
        float x = 0;
        float y = 0;
        float z = 0;
        float entityYaw = 0;

        this.bindEntityTexture(entity);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        int i = 0;
        float f = 0.0F;
        float f1 = 0.5F;
        float f2 = 0.0F;
        float f3 = 0.15625F;
        float f4 = 0.0F;
        float f5 = 0.15625F;
        float f6 = 0.15625F;
        float f7 = 0.3125F;
        float f8 = 0.05625F;
        GlStateManager.enableRescaleNormal();
        float f9 = (float)entity.arrowShake - partialTicks;

        if (f9 > 0.0F)
        {
            float f10 = -MathHelper.sin(f9 * 3.0F) * f9;
            GlStateManager.rotate(f10, 0.0F, 0.0F, 1.0F);
        }

        GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.05625F, 0.05625F, 0.05625F);
        GlStateManager.translate(-4.0F, 0.0F, 0.0F);

//        if (this.renderOutlines)
//        {
//            GlStateManager.enableColorMaterial();
//            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
//        }

        GlStateManager.glNormal3f(0.05625F, 0.0F, 0.0F);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(-7.0D, -2.0D, -2.0D).tex(0.0D, 0.15625D).endVertex();
        vertexbuffer.pos(-7.0D, -2.0D, 2.0D).tex(0.15625D, 0.15625D).endVertex();
        vertexbuffer.pos(-7.0D, 2.0D, 2.0D).tex(0.15625D, 0.3125D).endVertex();
        vertexbuffer.pos(-7.0D, 2.0D, -2.0D).tex(0.0D, 0.3125D).endVertex();
        tessellator.draw();
        GlStateManager.glNormal3f(-0.05625F, 0.0F, 0.0F);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos(-7.0D, 2.0D, -2.0D).tex(0.0D, 0.15625D).endVertex();
        vertexbuffer.pos(-7.0D, 2.0D, 2.0D).tex(0.15625D, 0.15625D).endVertex();
        vertexbuffer.pos(-7.0D, -2.0D, 2.0D).tex(0.15625D, 0.3125D).endVertex();
        vertexbuffer.pos(-7.0D, -2.0D, -2.0D).tex(0.0D, 0.3125D).endVertex();
        tessellator.draw();

        for (int j = 0; j < 4; ++j)
        {
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.glNormal3f(0.0F, 0.0F, 0.05625F);
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(-8.0D, -2.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
            vertexbuffer.pos(8.0D, -2.0D, 0.0D).tex(0.5D, 0.0D).endVertex();
            vertexbuffer.pos(8.0D, 2.0D, 0.0D).tex(0.5D, 0.15625D).endVertex();
            vertexbuffer.pos(-8.0D, 2.0D, 0.0D).tex(0.0D, 0.15625D).endVertex();
            tessellator.draw();
        }

//        if (this.renderOutlines)
//        {
//            GlStateManager.disableOutlineMode();
//            GlStateManager.disableColorMaterial();
//        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
