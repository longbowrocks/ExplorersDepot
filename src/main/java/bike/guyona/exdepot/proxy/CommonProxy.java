package bike.guyona.exdepot.proxy;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.config.ExDepotConfig;
import bike.guyona.exdepot.helpers.AccessHelpers;
import bike.guyona.exdepot.items.ItemRegistrar;
import bike.guyona.exdepot.network.*;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.capability.StorageConfigProvider;
import bike.guyona.exdepot.capability.StorageConfigStorage;
import bike.guyona.exdepot.gui.StorageConfigGuiHandler;
import bike.guyona.exdepot.sortingrules.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static bike.guyona.exdepot.ExDepotMod.*;
import static bike.guyona.exdepot.capability.StorageConfigProvider.STORAGE_CONFIG_CAPABILITY;
import static bike.guyona.exdepot.config.ExDepotConfig.keepConfigOnPickup;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.couldBeTileEntitySupported;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.isTileEntitySupported;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * Created by longb on 7/10/2017.
 */
public class CommonProxy {
    private int msgDiscriminator = 0;
    private Map<Vec3i, byte[]> pickedUpStorageConfigCache;
    ItemRegistrar itemRegistrar;
    public SortingRuleProvider sortingRuleProvider;
    public Map<String, Set<Integer>> modsAndCategoriesThatRegisterItems;

    public void preInit(FMLPreInitializationEvent event) {
        ExDepotConfig.configFile = new Configuration(event.getSuggestedConfigurationFile());
        ExDepotConfig.syncConfig();

        sortingRuleProvider = new SortingRuleProvider();
        AccessHelpers.setupCommonAccessors();

        itemRegistrar = new ItemRegistrar();
        MinecraftForge.EVENT_BUS.register(itemRegistrar);
    }

    public void init(FMLInitializationEvent event) {
        pickedUpStorageConfigCache = new HashMap<>();
        MinecraftForge.EVENT_BUS.register(this);
        NETWORK.registerMessage(
                StorageConfigCreateMessage.class,
                StorageConfigCreateMessage.class,
                msgDiscriminator++,
                Side.SERVER
        );
        NETWORK.registerMessage(
                StorageConfigCreateResponse.class,
                StorageConfigCreateResponse.class,
                msgDiscriminator++,
                Side.CLIENT
        );
        NETWORK.registerMessage(
                StoreItemsMessage.class,
                StoreItemsMessage.class,
                msgDiscriminator++,
                Side.SERVER
        );
        NETWORK.registerMessage(
                StoreItemsResponse.class,
                StoreItemsResponse.class,
                msgDiscriminator++,
                Side.CLIENT
        );
        NETWORK.registerMessage(
                StorageConfigRequestMessage.class,
                StorageConfigRequestMessage.class,
                msgDiscriminator++,
                Side.SERVER
        );
        NETWORK.registerMessage(
                StorageConfigRequestResponse.class,
                StorageConfigRequestResponse.class,
                msgDiscriminator++,
                Side.CLIENT
        );
        NETWORK.registerMessage( // Both this and the next message share a response message type.
                StorageConfigCreateFromChestMessage.class,
                StorageConfigCreateFromChestMessage.class,
                msgDiscriminator++,
                Side.SERVER
        );
        NETWORK.registerMessage( // Both this and the previous message share a response message type.
                StorageConfigSmartCreateFromChestMessage.class,
                StorageConfigSmartCreateFromChestMessage.class,
                msgDiscriminator++,
                Side.SERVER
        );
        NETWORK.registerMessage(
                StorageConfigCreateFromChestResponse.class,
                StorageConfigCreateFromChestResponse.class,
                msgDiscriminator++,
                Side.CLIENT
        );
        CapabilityManager.INSTANCE.register(StorageConfig.class, new StorageConfigStorage(), StorageConfig::new);
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new StorageConfigGuiHandler());
    }

    public void postInit(FMLPostInitializationEvent event) { }

    public void serverStarting(FMLServerStartingEvent event) {}

    public void serverStopping(FMLServerStoppingEvent event) {}

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if(eventArgs.getModID().equals(Ref.MODID)) {
            ExDepotConfig.syncConfig();
        }
    }

    @SubscribeEvent
    public void onTileCapabilityAttach(@NotNull AttachCapabilitiesEvent<TileEntity> event){
        if(couldBeTileEntitySupported(event.getObject())) {
            // Associate provider, which determines if TileEntity actually has the StorageConfig capability with hasCapability()
            event.addCapability(STORAGE_CONFIG_RSRC, new StorageConfigProvider());
        }
    }

    @SubscribeEvent
    public void onWorldTick(@NotNull TickEvent.WorldTickEvent event) {
        // break and itemization happen in the same tick after world tick, so clear cache every tick.
        pickedUpStorageConfigCache.clear();
    }

    //Cache StorageConfig for broken block so I can re-apply it when the block's drop is itemized.
    @SubscribeEvent
    public void handleBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof EntityPlayerMP && keepConfigOnPickup) {
            LOGGER.debug("Break occurred at: {}", event.getPlayer().getEntityWorld().getTotalWorldTime());
            TileEntity entity = event.getPlayer().getEntityWorld().getTileEntity(event.getPos());
            if (entity != null && isTileEntitySupported(entity)) {
                StorageConfig config = entity.getCapability(STORAGE_CONFIG_CAPABILITY, null);
                if (config != null) {
                    pickedUpStorageConfigCache.put(event.getPos(), config.toBytes());
                } else {
                    LOGGER.error("Somehow, config was null despite being registered");
                }
            }
        }
    }

    //Read StorageConfig from cache, into NBT.
    @SubscribeEvent
    public void handleBlockItemized(BlockEvent.HarvestDropsEvent event) {
        if (event.getHarvester() instanceof EntityPlayerMP) {
            LOGGER.debug("Itemize occurred at: {}", event.getWorld().getTotalWorldTime());
            if (pickedUpStorageConfigCache.containsKey(event.getPos())) {
                if (event.getDrops().size() == 1) {
                    ItemStack item = event.getDrops().get(0);
                    item.setTagInfo("storageConfigCache", new NBTTagByteArray(pickedUpStorageConfigCache.get(event.getPos())));
                    pickedUpStorageConfigCache.remove(event.getPos());
                } else {
                    LOGGER.error("Can only cache storage config for blocks that drop exactly 1 item. Harvest" +
                            " at {} Dropped {} items", event.getPos(), event.getDrops().size());
                }
            }
        }
    }

    //Read StorageConfig from NBT, back into TileEntity.
    @SubscribeEvent
    public void handleBlockPlaced(BlockEvent.PlaceEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player instanceof EntityPlayerMP) {
            ItemStack item = player.getHeldItem(event.getHand());
            NBTTagCompound ferriedValue = item.getTagCompound();
            if (ferriedValue != null) {
                byte[] bytes = ferriedValue.getByteArray("storageConfigCache");
                if (bytes.length == 0) {
                    return;
                }
                StorageConfig config = StorageConfig.fromBytes(bytes);
                TileEntity tile = player.getEntityWorld().getTileEntity(event.getPos());
                if (tile != null && isTileEntitySupported(tile)) {
                    StorageConfig baseConfig = tile.getCapability(STORAGE_CONFIG_CAPABILITY, null);
                    if (baseConfig != null) {
                        baseConfig.copyFrom(config);
                    } else {
                        LOGGER.error("Somehow, base config was null despite being registered");
                    }
                }
            }
        }
    }
}
