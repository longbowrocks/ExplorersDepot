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
    public void preInit(FMLPreInitializationEvent event) {}
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        NETWORK.registerMessage(
                StorageConfigMessage.StorageConfigMessageHandler.class,
                StorageConfigMessage.class,
                0,
                Side.SERVER
        );
        NETWORK.registerMessage(
                StoreItemsMessage.StoreItemsMessageHandler.class,
                StoreItemsMessage.class,
                1,
                Side.SERVER
        );
        CapabilityManager.INSTANCE.register(StorageConfig.class, new StorageConfigStorage(), StorageConfig.class);
    }
    public void postInit(FMLPostInitializationEvent event) {}
    public void serverStarting(FMLServerStartingEvent event) {}
    public void serverStopping(FMLServerStoppingEvent event) {}

//    public static void addConfig(int dimId, Chunk chunk, StorageConfig config){
//        String chunkName = String.format("%d,%d", chunk.xPosition, chunk.zPosition);
//        addConfig(dimId, chunkName, config);
//    }
//
//    public static void addConfig(int dimId, String chunkName, StorageConfig config){
//        instance.configMap.putIfAbsent(dimId, new HashMap<>());
//        instance.configMap.get(dimId).putIfAbsent(chunkName, new Vector<>());
//
//        instance.configMap.get(dimId).get(chunkName).add(config);
//    }

    @SubscribeEvent
    public void onCapabilityAttach(@NotNull AttachCapabilitiesEvent<TileEntity> event){
        if(event.getObject() instanceof TileEntityChest){
            event.addCapability(STORAGE_CONFIG_RSRC, new StorageConfigProvider());
        }
    }

//    @SubscribeEvent
//    public void onChunkDataLoad(@NotNull ChunkDataEvent.Load loadEvent){
//        int dimId = loadEvent.getWorld().provider.getDimension();
//        synchronized (this) {
//            //System.out.println("Chunk being loaded at: "+loadEvent.getChunk().getPos());
//            NBTTagCompound compound = loadEvent.getData();
//            String configsName = String.format("%s:storageConfSize", Ref.MODID);
//            int configCount = compound.getInteger(configsName);
//            for(int i=0; i < configCount; i++){
//                String configName = String.format("%s:storageConf%d", Ref.MODID, i);
//                byte[] bytes = compound.getByteArray(configName);
//                addConfig(dimId, loadEvent.getChunk(), StorageConfig.fromBytes(bytes));
//            }
//        }
//    }

//    @SubscribeEvent
//    public void onChunkDataSave(@NotNull ChunkDataEvent.Save saveEvent){
//        int dimId = saveEvent.getWorld().provider.getDimension();
//        synchronized (this) {
//            //System.out.println("Chunk being saved at: "+saveEvent.getChunk().getPos());
//            NBTTagCompound compound = saveEvent.getData();
//            if (instance.configMap.get(dimId) != null){
//                Chunk chunk = saveEvent.getChunk();
//                String chunkName = String.format("%d,%d", chunk.xPosition, chunk.zPosition);
//                if (instance.configMap.get(dimId).get(chunkName) != null) {
//                    Vector<StorageConfig> configVector = instance.configMap.get(dimId).get(chunkName);
//                    String configsName = String.format("%s:storageConfSize", Ref.MODID);
//                    compound.setInteger(configsName, configVector.size());
//                    for(int i=0; i < configVector.size(); i++){
//                        // Check that the chest's current chunk is the one we're saving.
//                        if(chunk != chunk.getWorld().getChunkFromBlockCoords(configVector.get(i).chest.getPos())){
//                            System.out.println("Chest has been moved. It is no longer in this chunk.");
//                            configVector.remove(i);
//                            i--;
//                            continue;
//                        }
//                        String configName = String.format("%s:storageConf%d", Ref.MODID, i);
//                        compound.setByteArray(configName, configVector.get(i).toBytes());
//                    }
//                    instance.configMap.get(dimId).remove(chunkName);
//                }
//                instance.configMap.remove(dimId);
//            }
//        }
//    }
}
