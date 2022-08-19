package bike.guyona.exdepot.client.particles;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.client.helpers.GuiHelpers;
import bike.guyona.exdepot.helpers.ChestFullness;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.resource.PathPackResources;
import net.minecraftforge.resource.ResourcePackLoader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static bike.guyona.exdepot.events.EventHandler.VIEWABLE_CONFIG_REFRESH_INTERVAL_MS;
import static net.minecraft.SharedConstants.TICKS_PER_SECOND;


// Some from https://github.com/longbowrocks/ExplorersDepot/compare/master...view_config_without_opening_it#diff-6bcbfc776a0b95590c9f26a81d340db82563cdfdf573b37e4a47e38e6d2b0b77R203
@OnlyIn(Dist.CLIENT)
public class ViewDepotParticle extends Particle {
    private final String modId;
    private final boolean simpleDepot;
    private final ChestFullness chestFullness;
    private final ResourceLocation backgroundPath = new ResourceLocation(Ref.MODID, "textures/particles/tablet_background.png");
    private ResourceLocation logoPath;

    public ViewDepotParticle(ClientLevel level, double x, double y, double z, String modId, boolean simpleDepot, ChestFullness chestFullness) {
        super(level, x, y, z);
        this.modId = modId;
        this.simpleDepot = simpleDepot;
        this.chestFullness = chestFullness;
        this.setSize(2,2);
        updateCache();

        final int GET_DEPOTS_LATENCY_TICKS = 1;
        final int MS_PER_SECOND = 1000;
        this.lifetime = VIEWABLE_CONFIG_REFRESH_INTERVAL_MS / MS_PER_SECOND * TICKS_PER_SECOND + GET_DEPOTS_LATENCY_TICKS;
        this.gravity = 0.0F;
    }

    /**
     * When you come back to this function, and it doesn't work, and you don't understand it,
     * re-read the miracle tutorial.
     * http://www.opengl-tutorial.org/beginners-tutorials/tutorial-3-matrices/
     * @param vertexConsumer
     * @param camera
     * @param partialTicks
     */
    @Override
    public void render(@NotNull VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            ExDepotMod.LOGGER.error("Rendered a depot view particle while player=null. wtf? Unloading particle...");
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
        posestack.translate(x,y,z); // Treat local coords as world coords.
        posestack.translate(-camPos.x, -camPos.y, -camPos.z); // Treat world coords as camera coords.
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

        RenderSystem.setShaderTexture(0, backgroundPath);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        Vec2 spriteOffset = getBackgroundSpriteSheetOffset(simpleDepot, chestFullness);
        float spriteSize = 0.25F;

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(bottomLeft.x,upperRight.y,bottomLeft.z).uv(spriteOffset.x,spriteOffset.y).endVertex();
        bufferbuilder.vertex(upperRight.x,upperRight.y,upperRight.z).uv(spriteOffset.x+spriteSize,spriteOffset.y).endVertex();
        bufferbuilder.vertex(upperRight.x,bottomLeft.y,upperRight.z).uv(spriteOffset.x+spriteSize,spriteOffset.y+spriteSize).endVertex();
        bufferbuilder.vertex(bottomLeft.x,bottomLeft.y,bottomLeft.z).uv(spriteOffset.x,spriteOffset.y+spriteSize).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        if (logoPath != null) {
            RenderSystem.setShaderTexture(0, logoPath);

            bufferbuilder = Tesselator.getInstance().getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.vertex(logoBottomLeft.x, logoUpperRight.y, logoBottomLeft.z).uv(0, 0).endVertex();
            bufferbuilder.vertex(logoUpperRight.x, logoUpperRight.y, logoUpperRight.z).uv(1, 0).endVertex();
            bufferbuilder.vertex(logoUpperRight.x, logoBottomLeft.y, logoUpperRight.z).uv(1, 1).endVertex();
            bufferbuilder.vertex(logoBottomLeft.x, logoBottomLeft.y, logoBottomLeft.z).uv(0, 1).endVertex();
            BufferUploader.drawWithShader(bufferbuilder.end());
        }

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
     * Given a point on the particle. Converts these values to the internal coordinate system of
     * the particle (in pixels), and returns a Vec3 with x and y set to the resulting values.
     * @param x
     * @param y
     * @return
     */
    private Vec3 getSeg(double x, double y) {
        x -= 0.5;
        y -= 0.5;
        return new Vec3(x*bbWidth, y*bbHeight, 0);
    }

    /**
     * Given some parameters describing which background should be used, return the sprite sheet coordinates for the
     * upper left of the correct background.
     * @param simpleDepot A simple depot is configured by mod rule, and only one mod rule.
     * @param chestFullness A chest can have room (0), have 80% slots full (1), or have 100% slots full (2)
     * @return a Vec2 representing the top left of the sprite we want to render, in spritesheet coords.
     * Spritesheet coordinates vary from 0 to 1.
     */
    private Vec2 getBackgroundSpriteSheetOffset(boolean simpleDepot, ChestFullness chestFullness) {
        float xOffset = 0;
        if (!simpleDepot) {
            xOffset += 0.25;
        }
        float yOffset = 0;
        if (chestFullness == ChestFullness.FILLING) {
            yOffset += 0.25;
        } else if (chestFullness == ChestFullness.FULL) {
            yOffset += 0.5;
        }
        return new Vec2(xOffset, yOffset);
    }

    private void updateCache() {
        if (modId == null || modId.isEmpty()) {
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
        final PathPackResources resourcePack = ResourcePackLoader.getPackFor(modId)
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
    }
}
