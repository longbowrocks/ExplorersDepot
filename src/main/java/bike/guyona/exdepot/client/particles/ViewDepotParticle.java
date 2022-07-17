package bike.guyona.exdepot.client.particles;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.client.helpers.GuiHelpers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector3d;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
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
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;


// Some from https://github.com/longbowrocks/ExplorersDepot/compare/master...view_config_without_opening_it#diff-6bcbfc776a0b95590c9f26a81d340db82563cdfdf573b37e4a47e38e6d2b0b77R203
@OnlyIn(Dist.CLIENT)
public class ViewDepotParticle extends Particle {
    private String modId;
    private ResourceLocation logoPath;
    private Size2i logoDims;

    public ViewDepotParticle(ClientLevel level, double x, double y, double z, String modId) {
        super(level, x, y, z);
        this.modId = modId;
        this.setSize(2,2);
        updateCache();

        this.lifetime = 40*TICKS_PER_SECOND;
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

        // Do global OpenGL configuration that's rolled back at end of the function.
        RenderSystem.depthMask(Minecraft.useShaderTransparency());
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(x,y,z); // Treat local coords as global coords.
        RenderSystem.applyModelViewMatrix();

        // Set and rotate particle in local coords.
        double playerX = player.xo + (player.getX() - player.xo) * (double)partialTicks;
        double playerZ = player.zo + (player.getZ() - player.zo) * (double)partialTicks;
        double deltaXFromChest = playerX - x;
        double deltaZFromChest = playerZ - z;
        float yawToChest = (float)Math.atan2(deltaXFromChest, deltaZFromChest);
        Vec3 bottomLeft = new Vec3(-bbWidth/2, -bbHeight/2, 0).yRot(yawToChest);
        Vec3 upperRight = new Vec3(bbWidth/2, bbHeight/2, 0).yRot(yawToChest);
        Vec3 logoBottomLeft = new Vec3(-bbWidth/3, -bbHeight/3, 0).yRot(yawToChest);
        Vec3 logoUpperRight = new Vec3(bbWidth/3, bbHeight/3, 0).yRot(yawToChest);

        ResourceLocation backgroundPath = new ResourceLocation(Ref.MODID, "textures/particles/tablet_background.png");
        RenderSystem.setShaderTexture(0, backgroundPath);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        Vec3 camPos = camera.getPosition();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        // Subtract camera to change to camera coords.
        bufferbuilder.vertex(bottomLeft.x - camPos.x,upperRight.y - camPos.y,bottomLeft.z - camPos.z).uv(0,0).endVertex();
        bufferbuilder.vertex(upperRight.x - camPos.x,upperRight.y - camPos.y,upperRight.z - camPos.z).uv(1,0).endVertex();
        bufferbuilder.vertex(upperRight.x - camPos.x,bottomLeft.y - camPos.y,upperRight.z - camPos.z).uv(1,1).endVertex();
        bufferbuilder.vertex(bottomLeft.x - camPos.x,bottomLeft.y - camPos.y,bottomLeft.z - camPos.z).uv(0,1).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);

        RenderSystem.setShaderTexture(0, logoPath);
        bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(logoBottomLeft.x - camPos.x,logoUpperRight.y - camPos.y,logoBottomLeft.z - camPos.z).uv(0,0).endVertex();
        bufferbuilder.vertex(logoUpperRight.x - camPos.x,logoUpperRight.y - camPos.y,logoUpperRight.z - camPos.z).uv(1,0).endVertex();
        bufferbuilder.vertex(logoUpperRight.x - camPos.x,logoBottomLeft.y - camPos.y,logoUpperRight.z - camPos.z).uv(1,1).endVertex();
        bufferbuilder.vertex(logoBottomLeft.x - camPos.x,logoBottomLeft.y - camPos.y,logoBottomLeft.z - camPos.z).uv(0,1).endVertex();
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
