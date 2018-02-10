package bike.guyona.exdepot.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import javax.annotation.Nullable;
import java.util.Set;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

public class ExDepotGuiFactory implements IModGuiFactory {
    @Override
    public void initialize(Minecraft minecraftInstance) {
        // TODO: this does something?
    }

    @Override
    public boolean hasConfigGui() {
        LOGGER.info("MAN DOES THIS DO ANYTHING");
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        LOGGER.info("MAN WHY DO WE NEED THIS WHEN WE ALREADY HAVE IT");
        return new ExDepotConfigGui(parentScreen);
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        LOGGER.info("MAN HERE WE GO AGAIN GETTING THAT DAMN CLASS");
        return ExDepotConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
}
