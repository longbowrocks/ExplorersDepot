package bike.guyona.exdepot.client.keys;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.network.deposititems.DepositItemsMessage;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static bike.guyona.exdepot.ExDepotMod.KEYBINDS;
import static bike.guyona.exdepot.ExDepotMod.NETWORK_INSTANCE;

@Mod.EventBusSubscriber(modid = Ref.MODID, value = Dist.CLIENT)
public class KeybindHandler {
    // When you swap to an item in your hotbar, its name fades in and out at the center of your hotbar.
    // If this variable is true, notifications show up like that instead of as chat messages.
    private static final boolean CENTER_NOTIFICATION = false;

    public static KeyMapping DEPOSIT_ITEMS_KEY;

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        DEPOSIT_ITEMS_KEY = registerKey(event,"deposit_items", KeyMapping.CATEGORY_GAMEPLAY, InputConstants.KEY_Z);
    }

    private static KeyMapping registerKey(RegisterKeyMappingsEvent event, String name, String category, int keycode) {
        KeyMapping key = new KeyMapping("key." + Ref.MODID + "." + name, keycode, category);
        event.register(key);
        return key;
    }

    @SubscribeEvent
    static void keyboardInputEvent(InputEvent.Key pressed) {
        if (inGameplayContext()) {
            handleGameplayEvent(pressed);
        }
    }

    private static void handleGameplayEvent(InputEvent.Key pressed) {
        // If event matches key, and key is not pressed, that means it's a keyUp event (I think).
        if (KEYBINDS.DEPOSIT_ITEMS_KEY.matches(pressed.getKey(), pressed.getScanCode()) && !KEYBINDS.DEPOSIT_ITEMS_KEY.isDown()) {
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
