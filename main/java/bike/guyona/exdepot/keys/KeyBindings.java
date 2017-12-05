package bike.guyona.exdepot.keys;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

/**
 * Created by longb on 9/9/2017.
 */
public class KeyBindings {

    public static KeyBinding dumpItems;

    public static void init() {
        dumpItems = new KeyBinding("Dump inventory to nearby chests", Keyboard.KEY_Z, "Utils");
        ClientRegistry.registerKeyBinding(dumpItems);
    }

}