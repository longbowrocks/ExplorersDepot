package bike.guyona.exdepot.proxy;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.gui.buttons.StorageConfigButton;
import bike.guyona.exdepot.gui.particle.ParticleFlyingItem;
import bike.guyona.exdepot.helpers.AccessHelpers;
import bike.guyona.exdepot.keys.KeyBindings;
import bike.guyona.exdepot.network.StoreItemsMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

import static bike.guyona.exdepot.ExDepotMod.instance;
import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;
import static bike.guyona.exdepot.Ref.INVTWEAKS_MIN_BUTTON_ID;
import static bike.guyona.exdepot.Ref.INVTWEAKS_NUM_BUTTONS;
import static bike.guyona.exdepot.Ref.STORAGE_CONFIG_BUTTON_ID;
import static bike.guyona.exdepot.gui.StorageConfigGuiHandler.STORAGE_CONFIG_GUI_ID;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.isGuiSupported;

/**
 * Created by longb on 7/10/2017.
 */
public class ClientProxy extends CommonProxy {
    private static final int TICKS_PER_ITEM_FLIGHT = 3;
    private int lastXsize = 0;
    private int lastYsize = 0;
    private int ticksSinceLastItemFlown = 0;
    private ConcurrentLinkedDeque<Map<BlockPos, List<ItemStack>>> sortedItems;
    private List<SoundEvent> itemStoredSounds;
    private final int STORE_ITEM_TONE_COUNT = 27;
    private int itemStoredCounter;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        sortedItems = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        KeyBindings.init();
        AccessHelpers.setupClientAccessors();
        itemStoredSounds = new Vector<>();
        for (int i=0; i<STORE_ITEM_TONE_COUNT; i++){
            ResourceLocation soundLocation = new ResourceLocation(Ref.MODID, "item_stored_" + (i+1));
            itemStoredSounds.add(new SoundEvent(soundLocation));
        }
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

    public void addSortingResults(Map<BlockPos, List<ItemStack>> sortingResults) {
        sortedItems.add(sortingResults);
    }

    private void chooseSortedItemToFly() {
        while (!sortedItems.isEmpty() && sortedItems.getFirst().isEmpty()) {
            sortedItems.pollFirst();
            itemStoredCounter = 0;
        }
        if (sortedItems.isEmpty()) {
            return;
        }
        if (++ticksSinceLastItemFlown >= TICKS_PER_ITEM_FLIGHT) {
            ticksSinceLastItemFlown = 0;
        } else {
            return;
        }
        Map<BlockPos, List<ItemStack>> currentSort = sortedItems.getFirst();
        BlockPos currentPos = null;
        for (BlockPos pos : currentSort.keySet()) {
            currentPos = pos;
            break;
        }
        addFlyingItem(currentSort.get(currentPos).get(0), currentPos);
        playFlyingClickSound();
        currentSort.get(currentPos).remove(0);
        if (currentSort.get(currentPos).size() == 0) {
            currentSort.remove(currentPos);
        }
    }

    public void addFlyingItem(ItemStack stack, BlockPos target) {
        World worldIn = Minecraft.getMinecraft().world;
        Particle particle = new ParticleFlyingItem(
                worldIn,
                Minecraft.getMinecraft().player.posX,
                Minecraft.getMinecraft().player.posY + 1.5,
                Minecraft.getMinecraft().player.posZ,
                target.getX() + 0.5,target.getY() + 0.5,target.getZ() + 0.5,
                stack);

        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }

    private void playFlyingClickSound() {
        Minecraft mc = Minecraft.getMinecraft();
        mc.world.playSound(mc.player.posX, mc.player.posY, mc.player.posZ, this.itemStoredSounds.get(itemStoredCounter%STORE_ITEM_TONE_COUNT), SoundCategory.PLAYERS, 1, 1, false);
        itemStoredCounter++;
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

            chooseSortedItemToFly();
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
