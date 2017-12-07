package bike.guyona.exdepot.proxy;

import bike.guyona.exdepot.storageconfig.*;
import bike.guyona.exdepot.storageconfig.capability.StorageConfig;
import bike.guyona.exdepot.storageconfig.capability.StorageConfigProvider;
import bike.guyona.exdepot.storageconfig.capability.StorageConfigStorage;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;

import static bike.guyona.exdepot.ExDepotMod.NETWORK;
import static bike.guyona.exdepot.ExDepotMod.STORAGE_CONFIG_RSRC;

/**
 * Created by longb on 7/10/2017.
 */
public class CommonProxy {
    private static int msgDiscriminator = 0;

    public void preInit(FMLPreInitializationEvent event) {}
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        NETWORK.registerMessage(
                StorageConfigMessage.StorageConfigMessageHandler.class,
                StorageConfigMessage.class,
                msgDiscriminator++,
                Side.SERVER
        );
        NETWORK.registerMessage(
                StoreItemsMessage.StoreItemsMessageHandler.class,
                StoreItemsMessage.class,
                msgDiscriminator++,
                Side.SERVER
        );
        NETWORK.registerMessage(
                StorageConfigRequestMessage.StorageConfigRequestMessageHandler.class,
                StorageConfigRequestMessage.class,
                msgDiscriminator++,
                Side.CLIENT
        );
        CapabilityManager.INSTANCE.register(StorageConfig.class, new StorageConfigStorage(), StorageConfig.class);
    }
    public void postInit(FMLPostInitializationEvent event) {}
    public void serverStarting(FMLServerStartingEvent event) {}
    public void serverStopping(FMLServerStoppingEvent event) {}

    @SubscribeEvent
    public void onCapabilityAttach(@NotNull AttachCapabilitiesEvent<TileEntity> event){
        if(event.getObject() instanceof TileEntityChest){
            event.addCapability(STORAGE_CONFIG_RSRC, new StorageConfigProvider());
        }
    }
}
