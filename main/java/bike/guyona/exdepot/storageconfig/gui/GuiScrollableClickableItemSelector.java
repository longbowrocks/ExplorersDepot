package bike.guyona.exdepot.storageconfig.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by longb on 12/7/2017.
 */
public class GuiScrollableClickableItemSelector extends GuiTextField {
    private List<Object> searchResults;

    public GuiScrollableClickableItemSelector(int componentId, FontRenderer fr, int x, int y, int width, int height, int maxHeight) {
        super(componentId, fr, x, y, width, height);
        searchResults = new ArrayList<>();
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        boolean keyTyped =  super.textboxKeyTyped(typedChar, keyCode);
        if (keyTyped) {
            updateSearchResults();
        }
        return keyTyped;
    }

    private void updateSearchResults() {
        Loader loader = Loader.instance();
        for(ModContainer mod : loader.getActiveModList()) {
            if (mod.getModId().startsWith(getText()) || mod.getName().startsWith(getText())) {
                searchResults.add(mod);
            }
        }
        // Either of these will get the item registry
        //GameRegistry.findRegistry(Item.class);
        //Item.REGISTRY;
        for(Item item:Item.REGISTRY) {
            //One way of getting items
        }
    }
}
