package bike.guyona.exdepot.proxy;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.gui.buttons.StorageConfigButton;
import bike.guyona.exdepot.keys.KeyBindings;
import bike.guyona.exdepot.network.StoreItemsMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


import static bike.guyona.exdepot.ExDepotMod.instance;
import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.Ref.INVTWEAKS_MIN_BUTTON_ID;
import static bike.guyona.exdepot.Ref.INVTWEAKS_NUM_BUTTONS;
import static bike.guyona.exdepot.Ref.STORAGE_CONFIG_BUTTON_ID;
import static bike.guyona.exdepot.gui.StorageConfigGuiHandler.STORAGE_CONFIG_GUI_ID;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.isGuiSupported;
import static net.minecraftforge.common.config.ConfigManager.sync;

/**
 * Created by longb on 7/10/2017.
 */
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        KeyBindings.init();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        super.serverStarting(event);
    }

    @Override
    public void serverStopping(FMLServerStoppingEvent event) {
        super.serverStopping(event);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Ref.MODID)) {
            sync(Ref.MODID, Config.Type.INSTANCE);
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(KeyBindings.dumpItems.isPressed()){
            ExDepotMod.NETWORK.sendToServer(new StoreItemsMessage());
        }
    }

    @SubscribeEvent
    public void onTick(@NotNull TickEvent.ClientTickEvent tick) {
        if(tick.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            if(mc.world != null && mc.player != null) {
                if(mc.currentScreen != null) {
                    onTickInGUI(mc.currentScreen);
                }
            }
        }
    }

    public static void openConfigurationGui(StorageConfig config) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.player.openGui(
                instance, STORAGE_CONFIG_GUI_ID, mc.world,
                (int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ
        );
        // Now set the storageConfig since it looks like I can't pass it as an arg
        // and I think client side GUI init is synchronous
        if (mc.currentScreen instanceof StorageConfigGui) {
            ((StorageConfigGui)mc.currentScreen).setStorageConfig(config);
        } else {
            LOGGER.error("There should have been a StorageConfigGui in mc.currentScreen. Instead got: "+
                    (mc.currentScreen == null ? "null" : mc.currentScreen.toString()));
        }
    }

    private void onTickInGUI(GuiScreen guiScreen){
        if(isGuiSupported(guiScreen)) {
            drawButton((GuiContainer) guiScreen);
        }
    }

    private void drawButton(GuiContainer guiChest){
        // Just remove the button every tick to make sure it's always placed right regardless of layout.
        guiChest.buttonList.removeIf(x -> x.id == STORAGE_CONFIG_BUTTON_ID);

        int buttonX = guiChest.getGuiLeft() + guiChest.getXSize() - 17, buttonY = guiChest.getGuiTop() + 5;
        for (GuiButton btn : guiChest.buttonList) {
            if (btn.id >= INVTWEAKS_MIN_BUTTON_ID && btn.id < INVTWEAKS_MIN_BUTTON_ID + INVTWEAKS_NUM_BUTTONS
                    && btn.x <= buttonX) {
                buttonX = btn.x - 12;
                buttonY = btn.y;
            }
        }
        guiChest.buttonList.add(
                new StorageConfigButton(STORAGE_CONFIG_BUTTON_ID, buttonX, buttonY,
                        10, 10));
    }
}
