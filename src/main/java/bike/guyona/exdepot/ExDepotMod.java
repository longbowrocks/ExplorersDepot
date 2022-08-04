package bike.guyona.exdepot;

import bike.guyona.exdepot.capabilities.CapabilityEventHandler;
import bike.guyona.exdepot.client.particles.DepositingItemParticleProvider;
import bike.guyona.exdepot.client.particles.ViewDepotParticleProvider;
import bike.guyona.exdepot.config.ExDepotConfig;
import bike.guyona.exdepot.items.DepotConfiguratorWandItem;
import bike.guyona.exdepot.keys.KeybindHandler;
import bike.guyona.exdepot.network.DepositItemsMessage;
import bike.guyona.exdepot.network.DepositItemsResponse;
import bike.guyona.exdepot.network.ViewDepotsMessage;
import bike.guyona.exdepot.network.ViewDepotsResponse;
import bike.guyona.exdepot.particles.DepositingItemParticleType;
import bike.guyona.exdepot.particles.ViewDepotParticleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static bike.guyona.exdepot.sounds.SoundEvents.*;


@Mod(Ref.MODID)
@Mod.EventBusSubscriber(modid = Ref.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ExDepotMod {
    /**
     * What is the goal of sorting?
     * Y = time spent searching for 1 or more items
     * Z = time spent depositing 1 or more items
     * the value of sorting: minimize(Y)
     * the cost of sorting: Z
     *
     * The value of this mod: minimize(Z+Y).
     * s = time spent determining the location of an item
     * r = time spent retrieving item
     * n = num items
     * Y = (s+r)*n
     *
     * t = time spent depositing item
     * Z = (s+t)*n
     *
     * How do you determine the location of an item.
     * If depositing, you have the items and must determine where it SHOULD go
     * This is an indexing problem, but most people don't know what indices to define.
     *
     * If retrieving, you have a memory of the item, or some constraints, and must determine where it IS.
     * This is just a general search problem.
     *
     * INDEXING:
     * index by properties that the user is likely to search by.
     * These are properties they are likely to recall, or properties that will be requested by their workflow.
     * One compelling feature of an indexing system is the ability to organize by new indexes as new patterns are discovered/tested
     *
     * SOLUTION:
     * 1. Do what I did before (allow making matchers for mod, item_category, mod+item_category, and item)
     * 2. Allow right-clicking with the wand to autoconfigure a chest.
     * 3. Show simplified configuration of a chest by holding wand (eg just mod logo, and an asterisk to indicate more).
     * 4. Show items traversing to the chests they were sorted into.
     *
     * This allows a simple interaction (right-click) to handle the 70% case, while also letting power users handle the last 30% to their own satisfaction.
     * Points 3 and 4 provide visual feedback to ensure people can tell at a glance where things are.
     */
    public static final Logger LOGGER = LogManager.getLogger(Ref.MODID);

    public static final ResourceLocation DEPOT_CAPABILITY_RESOURCE = new ResourceLocation(Ref.MODID, "depot_capability");

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Ref.MODID);
    public static final RegistryObject<Item> WAND_ITEM = ITEMS.register("depot_configurator_wand", () -> new DepotConfiguratorWandItem(new Item.Properties()));
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Ref.MODID);
    public static final RegistryObject<DepositingItemParticleType> DEPOSITING_ITEM_PARTICLE_TYPE = PARTICLE_TYPES.register("deposit_particle", DepositingItemParticleType::new);
    public static final RegistryObject<ViewDepotParticleType> VIEW_DEPOT_PARTICLE_TYPE = PARTICLE_TYPES.register("view_particle", ViewDepotParticleType::new);
    public static final KeybindHandler KEYBINDS = new KeybindHandler();
    public static final CapabilityEventHandler CAPABILITIES = new CapabilityEventHandler();

    private static final String NETWORK_PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK_INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Ref.MODID, "main_channel"),
            () -> NETWORK_PROTOCOL_VERSION,
            NETWORK_PROTOCOL_VERSION::equals,
            NETWORK_PROTOCOL_VERSION::equals
    );

    public ExDepotMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ExDepotConfig.SPEC);
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PARTICLE_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());

        // TODO: Need to manually add this listener because @SubscribeEvent is broken.
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(CAPABILITIES::registerCapabilities);

        int packetId = 0;
        NETWORK_INSTANCE.registerMessage(packetId++, DepositItemsMessage.class, DepositItemsMessage::encode, DepositItemsMessage::decode, DepositItemsMessage::handle);
        NETWORK_INSTANCE.registerMessage(packetId++, DepositItemsResponse.class, DepositItemsResponse::encode, DepositItemsResponse::decode, DepositItemsResponse::handle);
        NETWORK_INSTANCE.registerMessage(packetId++, ViewDepotsMessage.class, ViewDepotsMessage::encode, ViewDepotsMessage::decode, ViewDepotsMessage::handle);
        NETWORK_INSTANCE.registerMessage(packetId++, ViewDepotsResponse.class, ViewDepotsResponse::encode, ViewDepotsResponse::decode, ViewDepotsResponse::handle);
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("First: I am on side {}, second: Update log4j to >= 2.16", EffectiveSide.get());
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        KEYBINDS.registerKeys();

        Minecraft.getInstance().particleEngine.register(DEPOSITING_ITEM_PARTICLE_TYPE.get(), new DepositingItemParticleProvider());
        Minecraft.getInstance().particleEngine.register(VIEW_DEPOT_PARTICLE_TYPE.get(), new ViewDepotParticleProvider());
    }

    @SubscribeEvent
    static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        for (int i=0; i < NUM_DEPOSIT_SOUNDS; i++) {
            ResourceLocation loc = GameData.checkPrefix("item_stored_" + (i+1), false);
            SoundEvent sound = new SoundEvent(loc);
            sound.setRegistryName(loc);
            event.getRegistry().register(sound);
            DEPOSIT_SOUNDS.add(sound);
        }
    }
    // TODO: The docs are insistent that this code be isolated in a client-only area.
    //  For now, Imma trust that if the SERVER emits a PARTICLE registration event, it's ready to throw down some voodoo to make that happen.
    //  https://mcforge.readthedocs.io/en/1.18.x/gameeffects/particles/#creating-a-particle
    @SubscribeEvent
    static void registerParticleProviders(ParticleFactoryRegisterEvent event) {
    }
}
