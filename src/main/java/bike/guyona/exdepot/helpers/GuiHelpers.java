package bike.guyona.exdepot.helpers;

import bike.guyona.exdepot.gui.interfaces.IHasTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.ModContainer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;


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

    private static HashMap<String, ResourceLocation> modLogosCache;

    public static void drawMod(int left, int top, float zLevel, ModContainer mod, int width, int height) {
        if (modLogosCache == null) {
            modLogosCache = new HashMap<>();
        }
        BufferedImage logo = getModLogo(mod);
        if (logo != null)
        {
            Tessellator tess = Tessellator.getInstance();
            Minecraft mc = Minecraft.getMinecraft();
            TextureManager tm = mc.getTextureManager();
            ResourceLocation logoPath = modLogosCache.get(mod.getModId());
            if (logoPath == null) {
                logoPath = tm.getDynamicTextureLocation("modlogo", new DynamicTexture(logo));
                modLogosCache.put(mod.getModId(), logoPath);
            }
            Dimension logoDims = new Dimension(width, height);

            GlStateManager.enableBlend();
            mc.renderEngine.bindTexture(logoPath);
            BufferBuilder wr = tess.getBuffer();
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

    public static void drawTooltip(IHasTooltip tooltipObj, int x, int y, boolean drawLong) {
        drawTooltip(tooltipObj, x, y, drawLong,200);
    }

    public static void drawTooltip(IHasTooltip tooltipObj, int x, int y, boolean drawLong, int tooltipWidth) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String tooltip = drawLong ? tooltipObj.getLongTooltip() : tooltipObj.getTooltip();

        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen == null) {
            LOGGER.error("Current screen was null in drawTooltip. Current screen can't be null right now!");
            return;
        }
        if (tooltip == null) {
            return;
        }
        List<String> textLines = new ArrayList<>(Arrays.asList(tooltip.split("\n")));
        GuiUtils.drawHoveringText(textLines, x, y, currentScreen.width, currentScreen.height, tooltipWidth, fontRenderer);
    }
}
