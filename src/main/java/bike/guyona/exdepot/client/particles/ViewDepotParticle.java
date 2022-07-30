package bike.guyona.exdepot.client.particles;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.client.helpers.GuiHelpers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Size2i;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.resource.PathResourcePack;
import net.minecraftforge.resource.ResourcePackLoader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;


// Some from https://github.com/longbowrocks/ExplorersDepot/compare/master...view_config_without_opening_it#diff-6bcbfc776a0b95590c9f26a81d340db82563cdfdf573b37e4a47e38e6d2b0b77R203
@OnlyIn(Dist.CLIENT)
public class ViewDepotParticle extends Particle {
    public static final int COLOR_WHITE = 0xFFFFFF;

    private final String modId;
    private final ResourceLocation backgroundPath = new ResourceLocation(Ref.MODID, "textures/particles/tablet_background.png");
    private ResourceLocation logoPath;
    private Size2i logoDims;

    public ViewDepotParticle(ClientLevel level, double x, double y, double z, String modId) {
        super(level, x, y, z);
        this.modId = modId;
        this.setSize(2,2);
        updateCache();

        this.lifetime = 5*TICKS_PER_SECOND;
        this.gravity = 0.0F;
    }

    @Override
    public void render(@NotNull VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            ExDepotMod.LOGGER.error("Rendered a depot view particle while player=null. wtf? Unloading particle...");
            this.remove();
            return;
        }
        if (logoPath == null) {
            ExDepotMod.LOGGER.error("Rendered a depot view particle while logoPath=null. wtf? Unloading particle...");
            this.remove();
            return;
        }
        Vec3 camPos = camera.getPosition();

        // Do global OpenGL configuration that's rolled back at end of the function.
        RenderSystem.depthMask(Minecraft.useShaderTransparency());
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(x,y,z); // Treat local coords as global coords.
        posestack.translate(-camPos.x, -camPos.y, -camPos.z);
        RenderSystem.applyModelViewMatrix();

        // Set and rotate particle in local coords.
        double playerX = player.xo + (player.getX() - player.xo) * (double)partialTicks;
        double playerZ = player.zo + (player.getZ() - player.zo) * (double)partialTicks;
        double deltaXFromChest = playerX - x;
        double deltaZFromChest = playerZ - z;
        float yawToChest = (float)Math.atan2(deltaXFromChest, deltaZFromChest);
        Vec3 bottomLeft = getSeg(0,0).yRot(yawToChest);
        Vec3 upperRight = getSeg(1,1).yRot(yawToChest);
        Vec3 logoBottomLeft = getSeg(0.2,0.2).yRot(yawToChest);
        Vec3 logoUpperRight = getSeg(0.8,0.8).yRot(yawToChest);
        Vec3 extraBottomLeft = getSeg(0.8,0.8).yRot(yawToChest);
        Vec3 extraUpperRight = getSeg(0.9,0.9).yRot(yawToChest);

        RenderSystem.setShaderTexture(0, backgroundPath);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        // Subtract camera to change to camera coords.
        bufferbuilder.vertex(bottomLeft.x,upperRight.y,bottomLeft.z).uv(0,0).endVertex();
        bufferbuilder.vertex(upperRight.x,upperRight.y,upperRight.z).uv(1,0).endVertex();
        bufferbuilder.vertex(upperRight.x,bottomLeft.y,upperRight.z).uv(1,1).endVertex();
        bufferbuilder.vertex(bottomLeft.x,bottomLeft.y,bottomLeft.z).uv(0,1).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);

        RenderSystem.setShaderTexture(0, logoPath);
        bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(logoBottomLeft.x,logoUpperRight.y,logoBottomLeft.z).uv(0,0).endVertex();
        bufferbuilder.vertex(logoUpperRight.x,logoUpperRight.y,logoUpperRight.z).uv(1,0).endVertex();
        bufferbuilder.vertex(logoUpperRight.x,logoBottomLeft.y,logoUpperRight.z).uv(1,1).endVertex();
        bufferbuilder.vertex(logoBottomLeft.x,logoBottomLeft.y,logoBottomLeft.z).uv(0,1).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);

        int myColor = 0x20ffffff;
        int packedLightCoords = 0x00f00000;
        int backgroundOpacity = 0x3f000000;
        boolean withShadow = false;
        MultiBufferSource.BufferSource multibuffersource = MultiBufferSource.immediate(bufferbuilder);
        TextComponent nameTag = new TextComponent("Test Name");
        float xOffset = -25;
        float yOffset = 0;
        // The model+view matrix (ie RenderSystem.applyModelViewMatrix()) still applies to drawing text.
        // The matrix created here is a projection matrix, which is passed in and applied on top of
        // the model+view matrix.
        // If that makes no sense, read this: http://www.opengl-tutorial.org/beginners-tutorials/tutorial-3-matrices/
        // As they say at the top:
        // > This is the single most important tutorial of the whole set. Be sure to read it at least eight times.
        Matrix4f matrix4f = Matrix4f.createScaleMatrix(1,1,1);
        matrix4f.multiply(camera.rotation());
        matrix4f.multiply(Matrix4f.createScaleMatrix(-0.025F,-0.025F,0.025F));
        mc.font.drawInBatch(
                nameTag,
                xOffset,
                yOffset,
                myColor,
                withShadow,
                matrix4f,
                multibuffersource,
                true,
                backgroundOpacity,
                packedLightCoords
        );
        mc.font.drawInBatch(
                nameTag,
                xOffset,
                yOffset,
                -1,
                withShadow,
                matrix4f,
                multibuffersource,
                false,
                0,
                packedLightCoords
        );
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);

        // Roll back OpenGL configuration.
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    /**
     * Accepts two values, each between 0 and 1. Converts these values to the internal coordinate system of
     * the particle, and returns a Vec3 with x and y set to the resulting values.
     * @param x
     * @param y
     * @return
     */
    private Vec3 getSeg(double x, double y) {
        x -= 0.5;
        y -= 0.5;
        return new Vec3(x*bbWidth, y*bbHeight, 0);
    }

    private void updateCache() {
        if (modId == null) {
            return;
        }
        String logoFile = GuiHelpers.getModLogo(modId);
        Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modId);
        if (logoFile == null) {
            return;
        }
        if (modContainer.isEmpty()) {
            ExDepotMod.LOGGER.error("This is impossible. Mod container doesn't exist, despite being used to get logo.");
            return;
        }
        IModInfo selectedMod = modContainer.get().getModInfo();
        Minecraft mc = Minecraft.getInstance();

        TextureManager tm = mc.getTextureManager();
        final PathResourcePack resourcePack = ResourcePackLoader.getPackFor(modId)
                .orElse(ResourcePackLoader.getPackFor("forge").
                        orElseThrow(()->new RuntimeException("Can't find forge, WHAT!")));
        NativeImage logo;
        try {
            InputStream logoResource = resourcePack.getRootResource(logoFile);
            if (logoResource == null) {
                ExDepotMod.LOGGER.error("Logo file empty for mod: {}", modId);
                return;
            }
            logo = NativeImage.read(logoResource);
        }
        catch (IOException e) {
            ExDepotMod.LOGGER.error("Failed to read logo for mod: {}", modId);
            return;
        }

        logoPath = tm.register("modlogo", new DynamicTexture(logo) {
            @Override
            public void upload() {
                this.bind();
                NativeImage td = this.getPixels();
                // Use custom "blur" value which controls texture filtering (nearest-neighbor vs linear)
                this.getPixels().upload(0, 0, 0, 0, 0, td.getWidth(), td.getHeight(), selectedMod.getLogoBlur(), false, false, false);
            }
        });
        logoDims = new Size2i(logo.getWidth(), logo.getHeight());
    }
}
