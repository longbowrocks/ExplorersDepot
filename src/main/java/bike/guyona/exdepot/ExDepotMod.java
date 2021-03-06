package bike.guyona.exdepot;

import bike.guyona.exdepot.proxy.CommonProxy;
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
@Mod(modid=Ref.MODID, name=Ref.NAME, version=Ref.VERSION, dependencies="after:inventorytweaks",
        guiFactory = "bike.guyona.exdepot.config.ExDepotConfigGuiFactory")
public class ExDepotMod {
    @Mod.Instance
    public static ExDepotMod instance;
    @SidedProxy(clientSide=Ref.CLIENT_PROXY, serverSide=Ref.SERVER_PROXY)
    public static CommonProxy proxy;

    public static final Logger LOGGER = LogManager.getLogger(Ref.MODID);
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Ref.MODID);

    public static final ResourceLocation STORAGE_CONFIG_RSRC = new ResourceLocation(Ref.MODID, "storageconf");
    public static ResourceLocation MOD_BUTTON_TEXTURES = new ResourceLocation(Ref.MODID,"textures/gui/button_icons.png");

    /*
    MAIN TO-DO LIST
    xTODO: Let Nether Chest for 1.11 work
    TODO: Chests render as obsidian when flying.
    TODO: Can I make my mod load later to get other mods to register their item handlers first? That way I can check if a TileEntity has an Item Handler before deciding whether we support it.
    xTODO: Determine compatibility in one place
    TODO: Probably make the storageConfig what you see on screen. (Get rid of save button because it should always be saved?)
    TODO: Return empty StorageConfig instead of null
    TODO: Make configList and compatMode work again. That way you don't need to enable all TileEntities for this mod.
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
    xTODO: Filter by item health (eg red sand, or dyes)
    xTODO: Don't use numeric itemIds? Would only be useful for adding item health to end of itemId
    xTODO: Filter by regex. NOT USEFUL
    xTODO: Filter by item category
    xTODO: handle ironChests
    xTODO: make it easy to handle arbitrary chest types from other mods?
    xTODO: handle shulker chests
    xTODO: handle picking up shulker chests
    TODO: make it easy to craft something by grabbing from nearby chests?
    xTODO: remove inside jokes. replace with informative output to ONLY the user.
    xTODO: handle items that had their DisplayNames changed by anvil.
    xTODO: fix potion sorting rules to not do water bottle on reload. This may mean supporting NBT.
    xTODO: create common interface for sorting rules?
    xTODO: can't find potions with search bar.
    TODO: generalize rules so I can add new types easily.
    xTODO: localization
    TODO: change allItems from boolean to an AbstractSortingRule type.
    xTODO: config buttons should add rules for non-matching items to existing config, instead of adding new rules for all items in container.
    xTODO: move ruleClasses to ruleProvider.
    xTODO: Server probably needs work again. I've added loads of gui only code since I last tried it.
    xTODO: enable/disable mod for everything that makes (no) sense

    MESSAGES
    xTODO: StorageConfigCreateMessage: sent to server. Grab StorageConfig and add to cache.
    xTODO: StorageConfigRequestMessage: sent to server. Get SorageConfig from server.
    xTODO: StorageConfigRequestResponse: sent to client. Render StorageConfig to active GUIScreen
    xTODO: StoreItemsMessage: sent to server. iterate over player items, and store them by the rules in storageconfigs.

    OPTIMIZATION
    TODO: Move item storage on server to background thread.
    TODO: Move item search in UI to a background thread.
    xTODO: consider changing StorageConfig to use ordered set collections.
    xTODO: Stop rebasing versions > 1.11 on master branch for every change. While it does make the history look better, anyone who checks out the project may have troubles.

    COMPATIBILITY
    xTODO: Make this work on dedicated servers
    xTODO: Make this work with invtweaks
    xTODO: Make this work with NEI/JEI

    BUGS
    xTODO: Fix game freezing when you try to config a double chest
    xTODO: Fix double configs when you config a double chest from contents
    xTODO: Fix flickering caused by rendering items on an nvidia card
    xTODO: Fix some items like dirt and stone not showing up in search with improper caps
    xTODO: "di" returns redstone repeater in search bar
    xTODO: configuration not sticking?
    xTODO: storageConfig no longer saves.
    TODO: storage range is taken from server. I think I'd prefer to make it client side.
    TODO: glitzy flying items will probably crash if you unload the chunk or teleport to the nether (or any other worldspace)

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
    xTODO: update icons to make more sense/look better
    xTODO: question mark help GUI. Have it turn on advanced tooltips, that give more detail about each button you mouse over.
    xTODO: new button "generalize from contents", which makes its best guess at what rules you want.
    xTODO: useNbt icon.
    xTODO: draw tooltips after buttons.
    xTODO: put reminder somewhere in UI that tells users how rules are evaluated (rule panel adv tooltip?).
    xTODO: return null tooltip if you don't want to draw one.
    xTODO: colorize some more of the tooltips.
    xTODO: add ALL gui text to translation files. This may mean changing the way I do config.
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
}
