package bike.guyona.exdepot.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourcePack;
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


public class GuiHelpers {

    public static RenderItem getRenderItem() {
        return Minecraft.getMinecraft().getRenderItem();
    }

    public static void drawItem(int x, int y, ItemStack itemstack, FontRenderer fontRenderer) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();

        RenderItem itemRender = getRenderItem();
        itemRender.renderItemAndEffectIntoGUI(itemstack, x, y);
        itemRender.renderItemOverlayIntoGUI(fontRenderer, itemstack, x, y,
                "");

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
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
