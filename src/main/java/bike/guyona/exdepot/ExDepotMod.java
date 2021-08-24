package bike.guyona.exdepot;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.capability.StorageConfigProvider;
import bike.guyona.exdepot.capability.StorageConfigStorage;
import bike.guyona.exdepot.config.ExDepotConfig;
import bike.guyona.exdepot.event.EventHandler;
import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.gui.StorageConfigGuiHandler;
import bike.guyona.exdepot.gui.particle.ParticleFlyingItem;
import bike.guyona.exdepot.helpers.AccessHelpers;
import bike.guyona.exdepot.items.ItemConfigWand;
import bike.guyona.exdepot.keys.KeyBindings;
import bike.guyona.exdepot.network.*;
import bike.guyona.exdepot.sortingrules.SortingRuleProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

import static bike.guyona.exdepot.capability.StorageConfigProvider.STORAGE_CONFIG_CAPABILITY;
import static bike.guyona.exdepot.config.ExDepotConfig.keepConfigOnPickup;
import static bike.guyona.exdepot.event.EventHandler.STORE_ITEM_TONE_COUNT;
import static bike.guyona.exdepot.gui.StorageConfigGuiHandler.STORAGE_CONFIG_GUI_ID;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.couldBeTileEntitySupported;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.isTileEntitySupported;


/**
 * Created by longb on 7/12/2017.
 */
@Mod(Ref.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ExDepotMod {
    public static final Logger LOGGER = LogManager.getLogger(Ref.MODID);
    public static final String NETWORK_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Ref.MODID, "main"),
            () -> NETWORK_VERSION,
            NETWORK_VERSION::equals,
            NETWORK_VERSION::equals
    );

    public static final ResourceLocation STORAGE_CONFIG_RSRC = new ResourceLocation(Ref.MODID, "storageconf");
    public static ResourceLocation MOD_BUTTON_TEXTURES = new ResourceLocation(Ref.MODID,"textures/gui/button_icons.png");

    public static final SortingRuleProvider sortingRuleProvider = new SortingRuleProvider();

    public static DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, Ref.MODID);
    public static RegistryObject<ItemConfigWand> STORAGE_CONFIG_WAND =
            ITEM_REGISTRY.register("storage_configuration_wand", () -> new ItemConfigWand(new Item.Properties()));

    public static EventHandler EVENT_HANDLER = new EventHandler();

    // COMMON VARS
    private static int msgDiscriminator = 0;

    /*
    MAIN TO-DO LIST
    TODO: Can I make my mod load later to get other mods to register their item handlers first? That way I can check if a TileEntity has an Item Handler before deciding whether we support it.
    TODO: Probably make the storageConfig what you see on screen. (Get rid of save button because it should always be saved?)
    TODO: Return empty StorageConfig instead of null
    TODO: Make configList and compatMode work again. That way you don't need to enable all TileEntities for this mod.
    TODO: generalize rules so I can add new types easily.
    TODO: change allItems from boolean to an AbstractSortingRule type.


    OPTIMIZATION
    TODO: Move item storage on server to background thread.
    TODO: Move item search in UI to a background thread.

    BUGS
    TODO: storage range is taken from server. I think I'd prefer to make it client side.
    TODO: glitzy flying items will probably crash if you unload the chunk or teleport to the nether (or any other worldspace)
    TODO: Chests render as obsidian when flying.

     */

    public ExDepotMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ExDepotConfig.CLIENT_SPEC);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEM_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        AccessHelpers.setupCommonAccessors();
    }

    @SubscribeEvent
    public static void commonStart(FMLCommonSetupEvent event) {
        pickedUpStorageConfigCache = new HashMap<>();
        NETWORK.registerMessage(
                msgDiscriminator++,
                StorageConfigCreateMessage.class,
                StorageConfigCreateMessage::encode,
                StorageConfigCreateMessage::new,
                StorageConfigCreateMessage.Handler::onMessage
        );
        NETWORK.registerMessage(
                msgDiscriminator++,
                StorageConfigCreateResponse.class,
                StorageConfigCreateResponse::encode,
                StorageConfigCreateResponse::new,
                StorageConfigCreateResponse.Handler::onMessage
        );
        NETWORK.registerMessage(
                msgDiscriminator++,
                StoreItemsMessage.class,
                StoreItemsMessage::encode,
                StoreItemsMessage::new,
                StoreItemsMessage.Handler::onMessage
        );
        NETWORK.registerMessage(
                msgDiscriminator++,
                StoreItemsResponse.class,
                StoreItemsResponse::encode,
                StoreItemsResponse::new,
                StoreItemsResponse.Handler::onMessage
        );
        NETWORK.registerMessage( // Responds by sending a CreateGui message.
                msgDiscriminator++,
                StorageConfigRequestMessage.class,
                StorageConfigRequestMessage::encode,
                StorageConfigRequestMessage::new,
                StorageConfigRequestMessage.Handler::onMessage
        );
        NETWORK.registerMessage( // Responds by sending a CreateGui message.
                msgDiscriminator++,
                StorageConfigCreateFromChestMessage.class,
                StorageConfigCreateFromChestMessage::encode,
                StorageConfigCreateFromChestMessage::new,
                StorageConfigCreateFromChestMessage.Handler::onMessage
        );
        NETWORK.registerMessage( // Responds by sending a CreateGui message.
                msgDiscriminator++,
                StorageConfigSmartCreateFromChestMessage.class,
                StorageConfigSmartCreateFromChestMessage::encode,
                StorageConfigSmartCreateFromChestMessage::new,
                StorageConfigSmartCreateFromChestMessage.Handler::onMessage
        );
        CapabilityManager.INSTANCE.register(StorageConfig.class, new StorageConfigStorage(), StorageConfig::new);
        // TODO: can't do this anymore?
        // NetworkRegistry.INSTANCE.registerGuiHandler(instance, new StorageConfigGuiHandler());
    }

    @SubscribeEvent
    public static void registerContainers (RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(IForgeContainerType.create(MyContainerWrapper::new).setRegistryName("storage_config_container"));
    }

    // TODO: This runs on physical client. I want this to run on logical client. Probably.
    @SubscribeEvent
    private static void clientStart(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(new ContainerType<MyContainerWrapper>(), StorageConfigGui::new);

        EVENT_HANDLER.sortedItems = new ConcurrentLinkedDeque<>();

        KeyBindings.init();
        AccessHelpers.setupClientAccessors();
        EVENT_HANDLER.itemStoredSounds = new Vector<>();
        for (int i=0; i<STORE_ITEM_TONE_COUNT; i++){
            ResourceLocation soundLocation = new ResourceLocation(Ref.MODID, "item_stored_" + (i+1));
            EVENT_HANDLER.itemStoredSounds.add(new SoundEvent(soundLocation));
        }

        EventHandler.modsAndCategoriesThatRegisterItems = new HashMap<>();
        Function<? super String, ? extends Set<Integer>> mappingFunction = (k) -> new HashSet<>();
        for (Item item : ForgeRegistries.ITEMS) {  // TODO: if this is deprecated, what should I use?
            ResourceLocation res = item.getRegistryName();
            if (res != null) {
                modsAndCategoriesThatRegisterItems.computeIfAbsent(res.getNamespace(), mappingFunction);
                Set<Integer> categories = modsAndCategoriesThatRegisterItems.get(res.getNamespace());
                ItemGroup tab = AccessHelpers.getCreativeTab(item);
                if (tab != null) {
                    categories.add(tab.getIndex());
                }
            }
        }
    }
}
