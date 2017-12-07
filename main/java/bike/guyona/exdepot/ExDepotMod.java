package bike.guyona.exdepot;

import bike.guyona.exdepot.proxy.CommonProxy;
import bike.guyona.exdepot.storageconfig.StorageConfigButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Created by longb on 7/12/2017.
 */
@Mod(modid=Ref.MODID, name=Ref.NAME, version=Ref.VERSION)
public class ExDepotMod {
    @Mod.Instance
    public static ExDepotMod instance;
    @SidedProxy(clientSide=Ref.CLIENT_PROXY, serverSide=Ref.SERVER_PROXY)
    public static CommonProxy proxy;

    public static final Logger LOGGER = LogManager.getLogger(Ref.MODID);
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Ref.MODID);

    public static final ResourceLocation STORAGE_CONFIG_RSRC = new ResourceLocation(Ref.MODID, "storageconf");
    public static final String[] INSIDE_JOKES = {
            "Hagrid kills Dumbledore",
            "ERROR: Bow must be selected and drawn to de-nock arrows",
    };
    public static final int STORE_RANGE = 25;
    public boolean buttonAdded = false;

    /*
    MAIN TO-DO LIST
    xTODO: When a chunk is loaded, I should load all associated StorageConfigs in chunk NBT.
    xTODO: When a chunk is saved, I should save all associated StorageConfigs in chunk NBT.
    xTODO ALT: OR, I should load all StorageConfigs from world NBT, and translate chestPos to TileEntityChest references as chunks load.
    xTODO: Handle large chests. Maybe store a separate config data for each half?
    xTODO: What happens to a large chest when half of it is in an unloaded chunk? Handle this.
    xTODO: Handle chests moving between chunks. CANT HAPPEN BECAUSE CHESTS CANT BE PUSHED
    TODO: Handle picking up chests.
    xTODO: Instead of "Ping", Z handler should send a "StoreItems" message.
    xTODO: StoreItemsHandler should gather StorageConfigs within maxChestDist
    TODO: StoreItemsHandler should then build a chain of rules that can be run on each item in player inv to determine if they should be sent to a chest.
    TODO: StoreItemsHandler should then run the heuristic chain on each item in player inventory, top left to bottom right.

    MESSAGES
    xTODO: StorageConfigMessage: sent to server. Grab StorageConfig and add to cache.
    TODO: StorageConfigRequestMessage: sent to client. Render StorageConfig to active GUIScreen
    xTODO: StoreItemsMessage: sent to server. iterate over player items, and store them by the rules in storageconfigs.


    COMPATIBILITY
    TODO: Make this work on dedicated servers
    TODO: Make this work with invtweaks
    TODO: Make this work with NEI

    UI
    TODO: Need a StorageConfigMessage to client, so client can render storageConfig.
    TODO: Chest button should bring up a config GUI with an "All Items" toggle, a save button, and a clear button, so we can start saving configs.
    TODO: Config GUI should get a text box where I can enter item ids to accept.
    TODO: Config GUI should render rules in groups below text box.
    TODO: Config GUI should accept item names in place of item ids.
    TODO: Config GUI should accept modids and mod names.
     */

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        LOGGER.info(Ref.NAME+" Starting pre-init...");
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        LOGGER.info(Ref.NAME+" Starting init...");
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){
        LOGGER.info(Ref.NAME+" Starting post-init...");
        proxy.postInit(event);
    }

    public void onTickInGUI(GuiScreen guiScreen){
        if(guiScreen != null && guiScreen instanceof GuiChest) {
            drawButton((GuiChest) guiScreen);
        }
    }

    private void drawButton(GuiChest guiChest){
        if (!buttonAdded) {
            guiChest.buttonList.add(new StorageConfigButton(70,guiChest.getGuiLeft()+10, guiChest.getGuiTop(),"Test"));
            buttonAdded = true;
        }
    }
}
