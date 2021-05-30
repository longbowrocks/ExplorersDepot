package bike.guyona.exdepot;

import bike.guyona.exdepot.config.ExDepotConfig;
import bike.guyona.exdepot.items.ItemConfigWand;
import bike.guyona.exdepot.sortingrules.SortingRuleProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Created by longb on 7/12/2017.
 */
@Mod(Ref.MODID)
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

    /*
    MAIN TO-DO LIST
    TODO: Chests render as obsidian when flying.
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

     */

    public ExDepotMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ExDepotConfig.CLIENT_SPEC);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEM_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        NetworkHandler networkHandler = new NetworkHandler();
        DistExecutor.runWhenOn(Dist.CLIENT, ()->()-> clientStart(modEventBus, networkHandler));
        commonStart(modEventBus, networkHandler);
    }

    private static void clientStart(IEventBus modEventBus, NetworkHandler networkHandler) {
        EventBusHelper.addListener(modEventBus, ColorHandlerEvent.Block.class, setupEvent -> {
            Minecraft minecraft = Minecraft.getInstance();
            JeiSpriteUploader spriteUploader = new JeiSpriteUploader(minecraft.textureManager);
            Textures textures = new Textures(spriteUploader);
            IResourceManager resourceManager = minecraft.getResourceManager();
            if (resourceManager instanceof IReloadableResourceManager) {
                IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) resourceManager;
                reloadableResourceManager.addReloadListener(spriteUploader);
            }
            EventBusHelper.addLifecycleListener(modEventBus, FMLLoadCompleteEvent.class, loadCompleteEvent ->
                    new ClientLifecycleHandler(networkHandler, textures)
            );
        });
    }

    private static void commonStart(IEventBus modEventBus, NetworkHandler networkHandler) {
        EventBusHelper.addLifecycleListener(modEventBus, FMLCommonSetupEvent.class, event ->
                networkHandler.createServerPacketHandler()
        );
    }
}
