package bike.guyona.exdepot;

import bike.guyona.exdepot.capabilities.CapabilityEventHandler;
import bike.guyona.exdepot.client.particles.DepositingItemParticleProvider;
import bike.guyona.exdepot.client.particles.ViewDepotParticleProvider;
import bike.guyona.exdepot.config.ExDepotConfig;
import bike.guyona.exdepot.items.DepotConfiguratorWandItem;
import bike.guyona.exdepot.keys.KeybindHandler;
import bike.guyona.exdepot.loot.DepotPickerUpperLootModifier;
import bike.guyona.exdepot.loot.predicates.DepotCapableCondition;
import bike.guyona.exdepot.network.DepositItemsMessage;
import bike.guyona.exdepot.network.DepositItemsResponse;
import bike.guyona.exdepot.network.ViewDepotsMessage;
import bike.guyona.exdepot.network.ViewDepotsResponse;
import bike.guyona.exdepot.particles.DepositingItemParticleType;
import bike.guyona.exdepot.particles.ViewDepotParticleType;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
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

    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Ref.MODID);
    static {
        for (int i=0; i < NUM_DEPOSIT_SOUNDS; i++) {
            String name = "item_stored_" + (i+1);
            SoundEvent sound = new SoundEvent(new ResourceLocation(Ref.MODID, name));
            RegistryObject<SoundEvent> registeredSound = SOUND_EVENTS.register(name, () -> sound);
            DEPOSIT_SOUNDS.add(registeredSound);
        }
    }

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Ref.MODID);
    public static final RegistryObject<Item> WAND_ITEM = ITEMS.register("depot_configurator_wand", () -> new DepotConfiguratorWandItem(new Item.Properties()));

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Ref.MODID);
    public static final RegistryObject<DepositingItemParticleType> DEPOSITING_ITEM_PARTICLE_TYPE = PARTICLE_TYPES.register("deposit_particle", DepositingItemParticleType::new);
    public static final RegistryObject<ViewDepotParticleType> VIEW_DEPOT_PARTICLE_TYPE = PARTICLE_TYPES.register("view_particle", ViewDepotParticleType::new);

    public static LootItemConditionType DEPOT_CAPABLE_LOOT_CONDITION = new LootItemConditionType(DepotCapableCondition.SERIALIZER);

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Ref.MODID);
    public static final RegistryObject<Codec<DepotPickerUpperLootModifier>> DEPOT_PICKERUPPER_HOOK_LOOT_MODIFIER = GLOBAL_LOOT_MODIFIERS.register("depot_pickerupper_hook", () -> DepotPickerUpperLootModifier.CODEC);

    public static final String CAPABILITY_CACHE_KEY = String.format("%s:depot_capability_cache", Ref.MODID);

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
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ExDepotConfig.SPEC);
        ITEMS.register(bus);
        SOUND_EVENTS.register(bus);
        PARTICLE_TYPES.register(bus);
        GLOBAL_LOOT_MODIFIERS.register(bus);

        // TODO: Need to manually add this listener because @SubscribeEvent is broken.
        bus.addListener(CAPABILITIES::registerCapabilities);
        // TODO: this listener is also broken
        bus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(Registry.LOOT_CONDITION_TYPE.key())) {
                event.register(Registry.LOOT_CONDITION_TYPE.key(), DepotCapableCondition.ID, () -> DEPOT_CAPABLE_LOOT_CONDITION);
            }
        });

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

    // TODO: The docs are insistent that this code be isolated in a client-only area.
    //  For now, Imma trust that if the SERVER emits a PARTICLE registration event, it's ready to throw down some voodoo to make that happen.
    // https://docs.minecraftforge.net/en/1.19.x/gameeffects/particles/#particleprovider
    @SubscribeEvent
    static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.register(DEPOSITING_ITEM_PARTICLE_TYPE.get(), new DepositingItemParticleProvider());
        event.register(VIEW_DEPOT_PARTICLE_TYPE.get(), new ViewDepotParticleProvider());
    }

    @SubscribeEvent
    static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        KeybindHandler.onRegisterKeyMappings(event);
    }
}
