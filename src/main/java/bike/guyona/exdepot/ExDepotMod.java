package bike.guyona.exdepot;

import bike.guyona.exdepot.proxy.CommonProxy;
import bike.guyona.exdepot.gui.buttons.StorageConfigButton;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.StorageConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static bike.guyona.exdepot.Ref.*;
import static bike.guyona.exdepot.gui.StorageConfigGuiHandler.STORAGE_CONFIG_GUI_ID;


/**
 * Created by longb on 7/12/2017.
 */
@Mod(modid=Ref.MODID, name=Ref.NAME, version=Ref.VERSION, dependencies="after:inventorytweaks")
public class ExDepotMod {
    @Mod.Instance
    public static ExDepotMod instance;
    @SidedProxy(clientSide=Ref.CLIENT_PROXY, serverSide=Ref.SERVER_PROXY)
    public static CommonProxy proxy;

    public static final Logger LOGGER = LogManager.getLogger(Ref.MODID);
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Ref.MODID);

    public static final ResourceLocation STORAGE_CONFIG_RSRC = new ResourceLocation(Ref.MODID, "storageconf");
    public static ResourceLocation MOD_BUTTON_TEXTURES = new ResourceLocation(Ref.MODID,"textures/gui/button_icons.png");
    public static final String[] INSIDE_JOKES = {
            "Hagrid kills Dumbledore",
            "Bow must be selected and drawn to de-nock arrows",
            "Waygon",
            "...Adding sorting on item health in T minus NEVER.",
            "Re-acquiring Z keybind from heathen mods..."
    };

    /*
    MAIN TO-DO LIST
    xTODO: When a chunk is loaded, I should load all associated StorageConfigs in chunk NBT.
    xTODO: When a chunk is saved, I should save all associated StorageConfigs in chunk NBT.
    xTODO ALT: OR, I should load all StorageConfigs from world NBT, and translate chestPos to TileEntityChest references as chunks load.
    xTODO: Handle large chests. Maybe store a separate config data for each half?
    xTODO: What happens to a large chest when half of it is in an unloaded chunk? Handle this.
    xTODO: Handle chests moving between chunks. CANT HAPPEN BECAUSE CHESTS CANT BE PUSHED
    xTODO: Handle picking up chests. NAH, YOU PICK IT UP, IT'S GONE
    xTODO: Instead of "Ping", Z handler should send a "StoreItems" message.
    xTODO: StoreItemsHandler should gather StorageConfigs within maxChestDist
    xTODO: StoreItemsHandler should then build a chain of rules that can be run on each item in player inv to determine if they should be sent to a chest.
    xTODO: StoreItemsHandler should then run the heuristic chain on each item in player inventory, top left to bottom right.
    TODO: Allow sorting on item health
    TODO: Don't use numeric itemIds? Would only be useful for adding item health to end of itemId

    MESSAGES
    xTODO: StorageConfigCreateMessage: sent to server. Grab StorageConfig and add to cache.
    xTODO: StorageConfigRequestMessage: sent to server. Get SorageConfig from server.
    xTODO: StorageConfigRequestResponse: sent to client. Render StorageConfig to active GUIScreen
    xTODO: StoreItemsMessage: sent to server. iterate over player items, and store them by the rules in storageconfigs.

    OPTIMIZATION
    TODO: Move item storage on server to background thread.
    TODO: Move item search in UI to a background thread.

    COMPATIBILITY
    TODO: Make this work on dedicated servers
    xTODO: Make this work with invtweaks
    xTODO: Make this work with NEI/JEI

    UI
    xTODO: Need a StorageConfigCreateMessage to client, so client can render storageConfig.
    xTODO: Chest button should bring up a config GUI with an "All Items" toggle, a save button, and a clear button, so we can start saving configs.
    xTODO: make "AllItems" do something.
    xTODO: make "AllItems" show state with checkbox.
    xTODO: make "Save" do something.
    xTODO: make "Clear" do something.
    xTODO: make "FromInventory" do something.
    xTODO: Config GUI should get a text box where I can enter item ids to accept.
    xTODO: Config GUI should render rules in groups below text box.
    xTODO: Config GUI should accept item names in place of item ids.
    xTODO: Config GUI should accept modids and mod names.
    xTODO: fix storageconfig button to fit GUI, maybe don't have it say "TEST"
    xTODO: double check that storageconfig button is where I want it.
    xTODO: add settings page for changing storage distance
    xTODO: pull store key from config/settings instead of setting it to a const value at startup
    xTODO: make search suggestions disappear when deselected so you can edit rules
    xTODO: make ESC exit config gui
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
        // Just remove the button every tick to make sure it's always placed right regardless of layout.
        guiChest.buttonList.removeIf(x -> x.id == STORAGE_CONFIG_BUTTON_ID);

        int buttonX = guiChest.getGuiLeft() + guiChest.getXSize() - 17, buttonY = guiChest.getGuiTop() + 5;
        for (GuiButton btn : guiChest.buttonList) {
            if (btn.id >= INVTWEAKS_MIN_BUTTON_ID && btn.id < INVTWEAKS_MIN_BUTTON_ID + INVTWEAKS_NUM_BUTTONS
                    && btn.xPosition <= buttonX) {
                buttonX = btn.xPosition - 12;
                buttonY = btn.yPosition;
            }
        }
        guiChest.buttonList.add(
                new StorageConfigButton(STORAGE_CONFIG_BUTTON_ID, buttonX, buttonY,
                10, 10));
    }

    public static void openConfigurationGui(StorageConfig config) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.player.openGui(
                instance, STORAGE_CONFIG_GUI_ID, mc.world,
                (int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ
        );
        // Now set the storageConfig since it looks like I can't pass it as an arg
        // and I think client side GUI init is synchronous
        if (mc.currentScreen instanceof StorageConfigGui) {
            ((StorageConfigGui)mc.currentScreen).setStorageConfig(config);
        } else {
            LOGGER.error("There should have been a StorageConfigGui in mc.currentScreen. Instead got: "+
                    (mc.currentScreen == null ? "null" : mc.currentScreen.toString()));
        }
    }
}