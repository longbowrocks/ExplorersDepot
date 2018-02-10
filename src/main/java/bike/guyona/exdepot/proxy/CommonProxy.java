package bike.guyona.exdepot.proxy;

import bike.guyona.exdepot.network.*;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.capability.StorageConfigProvider;
import bike.guyona.exdepot.capability.StorageConfigStorage;
import bike.guyona.exdepot.gui.StorageConfigGuiHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;

import static bike.guyona.exdepot.ExDepotMod.NETWORK;
import static bike.guyona.exdepot.ExDepotMod.STORAGE_CONFIG_RSRC;
import static bike.guyona.exdepot.ExDepotMod.instance;

/**
 * Created by longb on 7/10/2017.
 */
public class CommonProxy {
    private static int msgDiscriminator = 0;

    public void preInit(FMLPreInitializationEvent event) {}
    public void init(FMLInitializationEvent event) {
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
        NETWORK.registerMessage( // This message type has no response
                StoreItemsMessage.class,
                StoreItemsMessage.class,
                msgDiscriminator++,
                Side.SERVER
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
        NETWORK.registerMessage(
                StorageConfigCreateFromChestMessage.class,
                StorageConfigCreateFromChestMessage.class,
                msgDiscriminator++,
                Side.SERVER
        );
        NETWORK.registerMessage(
                StorageConfigCreateFromChestResponse.class,
                StorageConfigCreateFromChestResponse.class,
                msgDiscriminator++,
                Side.CLIENT
        );
        CapabilityManager.INSTANCE.register(StorageConfig.class, new StorageConfigStorage(), StorageConfig.class);
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new StorageConfigGuiHandler());
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
