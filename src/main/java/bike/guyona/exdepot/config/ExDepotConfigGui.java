package bike.guyona.exdepot.config;

import bike.guyona.exdepot.Ref;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;
import java.util.Vector;

import static bike.guyona.exdepot.Ref.CATEGORY_MANUAL;

public class ExDepotConfigGui extends GuiConfig {
    public ExDepotConfigGui(GuiScreen parent) {
        super(parent,
                getElements(),
                Ref.MODID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(ExDepotConfig.configFile.toString()));
    }

    private static List<IConfigElement> getElements() {
        List<IConfigElement> list = new Vector<>();
        ConfigCategory mainElements = ExDepotConfig.configFile.getCategory(Configuration.CATEGORY_GENERAL);
        ConfigCategory manualElements = ExDepotConfig.configFile.getCategory(CATEGORY_MANUAL);

        list.addAll(new ConfigElement(mainElements).getChildElements());
        list.add(new DummyConfigElement.DummyCategoryElement("manualSettings",
                "exdepot.config.compatListSettings",
                new ConfigElement(manualElements).getChildElements()));
        return list;
    }
}
