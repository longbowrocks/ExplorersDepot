package bike.guyona.exdepot.proxy;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.config.ExDepotConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.gui.buttons.StorageConfigButton;
import bike.guyona.exdepot.gui.particle.ParticleFlyingItem;
import bike.guyona.exdepot.helpers.AccessHelpers;
import bike.guyona.exdepot.items.ItemRegistrar;
import bike.guyona.exdepot.keys.KeyBindings;
import bike.guyona.exdepot.network.StoreItemsMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
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
import static bike.guyona.exdepot.gui.StorageConfigGuiHandler.STORAGE_CONFIG_GUI_ID;

/**
 * Created by longb on 7/10/2017.
 */
public class ClientProxy extends CommonProxy {
    private static final int TICKS_PER_ITEM_FLIGHT = 3;
    private int ticksSinceLastItemFlown = 0;
    private ConcurrentLinkedDeque<Map<BlockPos, List<ItemStack>>> sortedItems;
    private List<SoundEvent> itemStoredSounds;
    private final int STORE_ITEM_TONE_COUNT = 27;
    private int itemStoredCounter;

    public Map<String,Integer> guiContainerAccessOrders;

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
        itemRegistrar.registerRenders();
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
        EntityPlayer player = Minecraft.getMinecraft().player;
        player.getForward();
        Particle particle = new ParticleFlyingItem(
                worldIn,
                player.posX, player.posY + player.eyeHeight, player.posZ,
                player.getForward(),
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
            chooseSortedItemToFly();
        }
    }

    public static void openConfigurationGui(StorageConfig config, BlockPos chestPos) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.player.openGui(
                instance, STORAGE_CONFIG_GUI_ID, mc.world,
                (int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ
        );
        // Now set the storageConfig since it looks like I can't pass it as an arg
        // and I think client side GUI init is synchronous
        if (mc.currentScreen instanceof StorageConfigGui) {
            ((StorageConfigGui)mc.currentScreen).setStorageConfig(config);
            ((StorageConfigGui)mc.currentScreen).setChestPosition(chestPos);
        } else {
            LOGGER.error("There should have been a StorageConfigGui in mc.currentScreen. Instead got: "+
                    (mc.currentScreen == null ? "null" : mc.currentScreen.toString()));
        }
    }
}
