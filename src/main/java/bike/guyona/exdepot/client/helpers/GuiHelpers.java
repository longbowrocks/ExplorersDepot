package bike.guyona.exdepot.client.helpers;

import bike.guyona.exdepot.ExDepotMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class GuiHelpers {
    public static String getModLogo(String modId) {
        Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modId);
        if (modContainer.isEmpty()) {
            ExDepotMod.LOGGER.error("Mod {} is not loaded.", modId);
            return null;
        }
        Optional<String> logoFile = modContainer.get().getModInfo().getLogoFile();
        if (logoFile.isEmpty()) {
            return null;
        }
        return logoFile.get();
    }
}
