package bike.guyona.exdepot.keys;

import bike.guyona.exdepot.Ref;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;

/**
 * Created by longb on 9/9/2017.
 */
public class KeyBindings {

    public static KeyBinding dumpItems;
    public static KeyBinding toggleMod;

    public static void init() {
        InputMappings.Input z_key_input = InputMappings.getInputByName("key.keyboard.z");
        dumpItems = new KeyBinding("Store Items", KeyConflictContext.IN_GAME, z_key_input, Ref.SHORT_NAME);
        toggleMod = new KeyBinding("Toggle ExDepot For Chest Type", KeyConflictContext.GUI, z_key_input, Ref.SHORT_NAME);
        ClientRegistry.registerKeyBinding(dumpItems);
        ClientRegistry.registerKeyBinding(toggleMod);
    }

}