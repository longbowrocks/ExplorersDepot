package bike.guyona.exdepot.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DepotRulesScreen extends Screen {
    public DepotRulesScreen(Component parentScreen) {
        super(Component.translatable("exdepot.gui.depotrules.title"));
    }

    /**
     * Initializes GUI state (ie widgets) from data state (ie DepotCapability).
     */
    @Override
    protected void init() {
        // No idea what I want here yet.
    }
}
