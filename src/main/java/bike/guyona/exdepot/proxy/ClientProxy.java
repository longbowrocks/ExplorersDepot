package bike.guyona.exdepot.proxy;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.gui.buttons.StorageConfigButton;
import bike.guyona.exdepot.helpers.AccessHelpers;
import bike.guyona.exdepot.keys.KeyBindings;
import bike.guyona.exdepot.network.StoreItemsMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static bike.guyona.exdepot.ExDepotMod.instance;
import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.Ref.INVTWEAKS_MIN_BUTTON_ID;
import static bike.guyona.exdepot.Ref.INVTWEAKS_NUM_BUTTONS;
import static bike.guyona.exdepot.Ref.STORAGE_CONFIG_BUTTON_ID;
import static bike.guyona.exdepot.gui.StorageConfigGuiHandler.STORAGE_CONFIG_GUI_ID;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.isGuiSupported;

/**
 * Created by longb on 7/10/2017.
 */
public class ClientProxy extends CommonProxy {
    private int lastXsize = 0;
    private int lastYsize = 0;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        KeyBindings.init();
        AccessHelpers.setupClientAccessors();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        modsAndCategoriesThatRegisterItems = new HashMap<>();
        Function<? super String, ? extends Set<Integer>> mappingFunction = (k) -> new HashSet<>();
        for (Item item : Item.REGISTRY) {
            ResourceLocation res = item.getRegistryName();
            if (res != null) {
                modsAndCategoriesThatRegisterItems.computeIfAbsent(res.getResourceDomain(), mappingFunction);
                Set<Integer> categories = modsAndCategoriesThatRegisterItems.get(res.getResourceDomain());
                CreativeTabs tab = AccessHelpers.getCreativeTab(item);
                if (tab != null) {
                    categories.add(tab.getTabIndex());
                }
            }
        }
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
            addStorageConfigButton((GuiContainer) guiScreen);
        }
    }

    private void addStorageConfigButton(GuiContainer guiChest){
        List<GuiButton> buttonList = AccessHelpers.getButtonList(guiChest);
        if (buttonList == null) {
            LOGGER.error("This isn't maintainable.");
            return;
        }

        boolean buttonAlreadyAdded = false;
        for (GuiButton btn : buttonList) {
            if (btn.id == STORAGE_CONFIG_BUTTON_ID)
                buttonAlreadyAdded = true;
        }
        if (buttonAlreadyAdded && (guiChest.getXSize() != lastXsize || guiChest.getYSize() != lastYsize)) {
            return;
        }
        lastXsize = guiChest.getXSize();
        lastYsize = guiChest.getYSize();

        buttonList.removeIf(x -> x.id == STORAGE_CONFIG_BUTTON_ID);

        int buttonX = guiChest.getGuiLeft() + guiChest.getXSize() - 17;
        int buttonY = guiChest.getGuiTop() + 5;
        // TODO: add proper code to handle ironchests and other mods that may not provide space for my button.
        if (guiChest.getYSize() == 184 || guiChest.getYSize() == 202 || guiChest.getYSize() == 238 || guiChest.getYSize() == 256) {
            buttonX += 5;
        }

        int minX = Integer.MAX_VALUE;
        int maxY = 0;
        boolean hasInvTweaks = false;
        boolean orientationIsHorizontal = false;
        for (GuiButton btn : buttonList) {
            if (btn.id >= INVTWEAKS_MIN_BUTTON_ID && btn.id < INVTWEAKS_MIN_BUTTON_ID + INVTWEAKS_NUM_BUTTONS) {
                if (!hasInvTweaks) {
                    hasInvTweaks = true;
                    minX = btn.xPosition;
                    maxY = btn.yPosition;
                    continue;
                }
                if (maxY == btn.yPosition) {
                    orientationIsHorizontal = true;
                }
                if (btn.yPosition > maxY) {
                    maxY = btn.yPosition;
                }
                if (btn.xPosition < minX) {
                    minX = btn.xPosition;
                }
            }
        }
        if (hasInvTweaks) {
            if (orientationIsHorizontal) {
                buttonX = minX - 12;
                buttonY = maxY;
            } else {
                buttonX = minX;
                buttonY = maxY + 12;
            }
        }
        buttonList.add(
                new StorageConfigButton(STORAGE_CONFIG_BUTTON_ID, buttonX, buttonY,
                        10, 10));
    }
}
