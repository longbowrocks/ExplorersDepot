package bike.guyona.exdepot.config;

import bike.guyona.exdepot.Ref;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

public class ExDepotConfigGui extends GuiConfig {
    public ExDepotConfigGui(GuiScreen parent) {
        super(parent,
                new ConfigElement(ExDepotConfig.configFile.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
                Ref.MODID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(ExDepotConfig.configFile.toString()));
    }
}
