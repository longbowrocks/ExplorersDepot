package bike.guyona.exdepot.config;

import bike.guyona.exdepot.Ref;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

public class ExDepotConfigGui extends GuiConfig {
    public ExDepotConfigGui(GuiScreen parentScreen) {
        super(parentScreen, Ref.MODID, Ref.NAME);
    }
}
