package bike.guyona.exdepot.client.keys;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.items.DepotConfiguratorWandBase;
import bike.guyona.exdepot.network.deposititems.DepositItemsMessage;
import bike.guyona.exdepot.network.wandmodechanged.ChangeWandModeMessage;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
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

    public static KeyMapping DEPOSIT_ITEMS_KEY;

    public static void onRegisterKeyMappings() {
        DEPOSIT_ITEMS_KEY = registerKey("deposit_items", KeyMapping.CATEGORY_GAMEPLAY, InputConstants.KEY_Z);
    }

    private static KeyMapping registerKey(String name, String category, int keycode) {
        KeyMapping key = new KeyMapping("key." + Ref.MODID + "." + name, keycode, category);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }

    @SubscribeEvent
    static void keyboardInputEvent(InputEvent.KeyInputEvent pressed) {
        if (inGameplayContext()) {
            handleGameplayKeyPress(pressed);
        }
    }

    @SubscribeEvent
    static void mouseInputEvent(InputEvent.MouseScrollEvent mouseEvent) {
        if (inGameplayContext()) {
            handleGameplayMouseScroll(mouseEvent);
        }
    }

    private static void handleGameplayKeyPress(InputEvent.KeyInputEvent pressed) {
        // If event matches key, and key is not pressed, that means it's a keyUp event (I think).
        if (KEYBINDS.DEPOSIT_ITEMS_KEY.matches(pressed.getKey(), pressed.getScanCode()) && !KEYBINDS.DEPOSIT_ITEMS_KEY.isDown()) {
            NETWORK_INSTANCE.sendToServer(new DepositItemsMessage());
        }
    }

    private static void handleGameplayMouseScroll(InputEvent.MouseScrollEvent mouseEvent) {
        Minecraft mc = Minecraft.getInstance();
        ItemStack mainHandItem = mc.player == null ? ItemStack.EMPTY : mc.player.getMainHandItem();
        if (mc.options.keyShift.isDown() && mouseEvent.getScrollDelta() != 0 && DepotConfiguratorWandBase.isWand(mainHandItem.getItem())) {
            int direction = mouseEvent.getScrollDelta() > 0 ? 1 : -1;
            NETWORK_INSTANCE.sendToServer(new ChangeWandModeMessage(direction));
            mouseEvent.setCanceled(true);
        }
    }

    private static boolean inGameplayContext() {
        return inGame() && Minecraft.getInstance().screen == null;
    }

    private static boolean inGame() {
        return Minecraft.getInstance() != null && Minecraft.getInstance().level != null;
    }
}
