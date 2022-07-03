package bike.guyona.exdepot.keys;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.network.DepositItemsMessage;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static bike.guyona.exdepot.ExDepotMod.KEYBINDS;
import static bike.guyona.exdepot.ExDepotMod.NETWORK_INSTANCE;

@Mod.EventBusSubscriber(modid = Ref.MODID, value = Dist.CLIENT)
public class KeybindHandler {
    // When you swap to an item in your hotbar, its name fades in and out at the center of your hotbar.
    // If this variable is true, notifications show up like that instead of as chat messages.
    private static final boolean CENTER_NOTIFICATION = false;

    public KeyMapping depositItemsKey;

    public void registerKeys() {
        depositItemsKey = registerKey("deposit_items", KeyMapping.CATEGORY_GAMEPLAY, InputConstants.KEY_Z);
    }

    private KeyMapping registerKey(String name, String category, int keycode) {
        KeyMapping key = new KeyMapping("key." + Ref.MODID + "." + name, keycode, category);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }

    @SubscribeEvent
    static void keyboardInputEvent(InputEvent.KeyInputEvent pressed) {
        if (inGameplayContext()) {
            handleGameplayEvent(pressed);
        }
    }

    private static void handleGameplayEvent(InputEvent.KeyInputEvent pressed) {
        // If event matches key, and key is not pressed, that means it's a keyUp event (I think).
        if (KEYBINDS.depositItemsKey.matches(pressed.getKey(), pressed.getScanCode()) && !KEYBINDS.depositItemsKey.isDown()) {
            NETWORK_INSTANCE.sendToServer(new DepositItemsMessage());
        }
    }

    private static boolean inGameplayContext() {
        return inGame() && Minecraft.getInstance().screen == null;
    }

    private static boolean inGame() {
        return Minecraft.getInstance() != null && Minecraft.getInstance().level != null;
    }
}
