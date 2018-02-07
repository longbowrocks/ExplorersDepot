package bike.guyona.exdepot.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.ModContainer;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

public class GuiHelpers {
    private static final int MODEL_VIEW_DEPTH = -1;

    public static RenderItem getRenderItem() {
        return Minecraft.getMinecraft().getRenderItem();
    }

    public static void enable3DRender() {
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    public static void enable2DRender() {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
    }

    public static boolean checkMatrixStack() {
        return MODEL_VIEW_DEPTH < 0 || GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH) == MODEL_VIEW_DEPTH;
    }

    public static void restoreMatrixStack() {
        if (MODEL_VIEW_DEPTH >= 0) {
            for (int i = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH); i > MODEL_VIEW_DEPTH; i--) {
                GlStateManager.popMatrix();
            }
        }
    }

    public static void drawItem(int i, int j, ItemStack itemstack, FontRenderer fontRenderer) {
        enable3DRender();
        RenderItem drawItems = getRenderItem();
        float zLevel = drawItems.zLevel += 100F;
        try {
            drawItems.renderItemAndEffectIntoGUI(itemstack, i, j);
            drawItems.renderItemOverlays(fontRenderer, itemstack, i, j);

            if (!checkMatrixStack()) {
                throw new IllegalStateException("Modelview matrix stack too deep");
            }
            if (Tessellator.getInstance().getBuffer().isDrawing) {
                throw new IllegalStateException("Still drawing");
            }
        } catch (Exception e) {
            LOGGER.error("Error while rendering: " + itemstack);

            restoreMatrixStack();
            if (Tessellator.getInstance().getBuffer().isDrawing) {
                Tessellator.getInstance().draw();
            }

            drawItems.zLevel = zLevel;
            drawItems.renderItemIntoGUI(new ItemStack(Blocks.STONE), i, j);
        }

        enable2DRender();
        drawItems.zLevel = zLevel - 100;
    }

    public static BufferedImage getModLogo(ModContainer mod) {
        Minecraft mc = Minecraft.getMinecraft();
        BufferedImage logo = null;

        String logoFile = mod.getMetadata().logoFile;
        if (!logoFile.isEmpty())
        {
            IResourcePack pack = FMLClientHandler.instance().getResourcePackFor(mod.getModId());
            try
            {
                if (pack != null)
                {
                    logo = pack.getPackImage();
                }
                else
                {
                    InputStream logoResource = new Object().getClass().getResourceAsStream(logoFile);
                    if (logoResource != null)
                        logo = ImageIO.read(logoResource);
                }
            }
            catch (IOException e) {}
        }
        return logo;
    }

    public static void drawMod(int left, int top, float zLevel, ModContainer mod, int width, int height) {
        BufferedImage logo = getModLogo(mod);
        if (logo != null)
        {
            Tessellator tess = Tessellator.getInstance();
            Minecraft mc = Minecraft.getMinecraft();
            TextureManager tm = mc.getTextureManager();
            ResourceLocation logoPath = tm.getDynamicTextureLocation("modlogo", new DynamicTexture(logo));
            Dimension logoDims = new Dimension(width, height);

            GlStateManager.enableBlend();
            mc.renderEngine.bindTexture(logoPath);
            VertexBuffer wr = tess.getBuffer();
            int offset = left;
            wr.begin(7, DefaultVertexFormats.POSITION_TEX);
            wr.pos(offset,                  top + logoDims.height, zLevel).tex(0, 1).endVertex();
            wr.pos(offset + logoDims.width, top + logoDims.height, zLevel).tex(1, 1).endVertex();
            wr.pos(offset + logoDims.width, top,                   zLevel).tex(1, 0).endVertex();
            wr.pos(offset,                  top,                   zLevel).tex(0, 0).endVertex();
            tess.draw();
            GlStateManager.disableBlend();
        }
    }
}
