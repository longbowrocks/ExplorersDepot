package bike.guyona.exdepot.client.helpers;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
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
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class GuiHelpers {
    private static final Map<String, ResourceLocation> MOD_LOGO_REGISTRY_ENTRIES = new HashMap<>();

    @NotNull
    private static ResourceLocation getKnownTexture(@NotNull String logoFilename, @NotNull String registryName) {
        Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(Ref.MODID);
        if (modContainer.isEmpty()) {
            ExDepotMod.LOGGER.error("IMPOSSIBLE: Mod {} is reporting that Mod {} is not loaded.", Ref.MODID, Ref.MODID);
            throw new RuntimeException("IMPOSSIBLE: Mod can't find itself.");
        }
        IModInfo selectedMod = modContainer.get().getModInfo();
        Minecraft mc = Minecraft.getInstance();

        TextureManager tm = mc.getTextureManager();
        final PathPackResources resourcePack = ResourcePackLoader.getPackFor(Ref.MODID).orElse(null);
        if (resourcePack == null) {
            ExDepotMod.LOGGER.error("IMPOSSIBLE: resource pack not found for mod: {}", Ref.MODID);
            throw new RuntimeException("IMPOSSIBLE: Mod can't find its own resource pack.");
        }
        NativeImage logo;
        try {
            InputStream logoResource = resourcePack.getRootResource(logoFilename);
            if (logoResource == null) {
                ExDepotMod.LOGGER.error("IMPOSSIBLE: {} empty for mod: {}", logoFilename, Ref.MODID);
                throw new RuntimeException("IMPOSSIBLE: Can't find known texture.");
            }
            logo = NativeImage.read(logoResource);
        }
        catch (IOException e) {
            ExDepotMod.LOGGER.error("IMPOSSIBLE: Failed to read {} for mod: {}", logoFilename, Ref.MODID);
            throw new RuntimeException("IMPOSSIBLE: Can't read known texture.");
        }

        ResourceLocation dynamicRegistryName = tm.register(registryName, new DynamicTexture(logo) {
            @Override
            public void upload() {
                this.bind();
                NativeImage td = this.getPixels();
                // Use custom "blur" value which controls texture filtering (nearest-neighbor vs linear)
                this.getPixels().upload(0, 0, 0, 0, 0, td.getWidth(), td.getHeight(), selectedMod.getLogoBlur(), false, false, false);
            }
        });
        MOD_LOGO_REGISTRY_ENTRIES.put(registryName, dynamicRegistryName);
        return dynamicRegistryName;
    }

    @NotNull
    public static ResourceLocation registerModLogoTexture(@NotNull String modId) {
        String registryName = String.format("%s_modlogo_%s", Ref.MODID, modId);
        if (MOD_LOGO_REGISTRY_ENTRIES.containsKey(registryName)) {
            ExDepotMod.LOGGER.debug("Using registry for {}", registryName);
            return MOD_LOGO_REGISTRY_ENTRIES.get(registryName);
        }

        if (modId.equals("minecraft")) {
            return getKnownTexture("mc_logo.png", registryName);
        }
        Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modId);
        if (modContainer.isEmpty()) {
            ExDepotMod.LOGGER.warn("No mod container found for {}.", modId);
            return getKnownTexture("question_mark.png", registryName);
        }
        IModInfo selectedMod = modContainer.get().getModInfo();
        Minecraft mc = Minecraft.getInstance();

        TextureManager tm = mc.getTextureManager();
        final PathPackResources resourcePack = ResourcePackLoader.getPackFor(modId).orElse(null);
        if (resourcePack == null) {
            ExDepotMod.LOGGER.warn("No resource pack found for {}.", modId);
            return getKnownTexture("question_mark.png", registryName);
        }
        Optional<String> logoFile = modContainer.get().getModInfo().getLogoFile();
        if (logoFile.isEmpty()) {
            ExDepotMod.LOGGER.warn("No logo filename found for {}.", modId);
            return getKnownTexture("question_mark.png", registryName);
        }
        NativeImage logo;
        try {
            InputStream logoResource = resourcePack.getRootResource(logoFile.get());
            if (logoResource == null) {
                ExDepotMod.LOGGER.warn("Logo file empty for mod: {}", modId);
                return getKnownTexture("question_mark.png", registryName);
            }
            logo = NativeImage.read(logoResource);
        }
        catch (IOException e) {
            ExDepotMod.LOGGER.warn("Failed to read logo for mod: {}", modId);
            return getKnownTexture("question_mark.png", registryName);
        }

        ResourceLocation dynamicRegistryName = tm.register(registryName, new DynamicTexture(logo) {
            @Override
            public void upload() {
                this.bind();
                NativeImage td = this.getPixels();
                // Use custom "blur" value which controls texture filtering (nearest-neighbor vs linear)
                this.getPixels().upload(0, 0, 0, 0, 0, td.getWidth(), td.getHeight(), selectedMod.getLogoBlur(), false, false, false);
            }
        });
        MOD_LOGO_REGISTRY_ENTRIES.put(registryName, dynamicRegistryName);
        return dynamicRegistryName;
    }
}
