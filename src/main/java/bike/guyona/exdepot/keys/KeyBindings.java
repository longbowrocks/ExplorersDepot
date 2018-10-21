package bike.guyona.exdepot.keys;

import bike.guyona.exdepot.Ref;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

/**
 * Created by longb on 9/9/2017.
 */
public class KeyBindings {

    public static KeyBinding dumpItems;
    public static KeyBinding toggleMod;

    public static void init() {
        dumpItems = new KeyBinding("Store Items", KeyConflictContext.IN_GAME, Keyboard.KEY_Z, Ref.SHORT_NAME);
        toggleMod = new KeyBinding("Toggle ExDepot For Chest Type", KeyConflictContext.GUI, Keyboard.KEY_Z, Ref.SHORT_NAME);
        ClientRegistry.registerKeyBinding(dumpItems);
        ClientRegistry.registerKeyBinding(toggleMod);
    }

}