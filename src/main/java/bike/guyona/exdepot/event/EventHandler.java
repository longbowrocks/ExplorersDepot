package bike.guyona.exdepot.event;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.capability.StorageConfigProvider;
import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.gui.particle.ParticleFlyingItem;
import bike.guyona.exdepot.keys.KeyBindings;
import bike.guyona.exdepot.network.StoreItemsMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

import static bike.guyona.exdepot.ExDepotMod.STORAGE_CONFIG_RSRC;
import static bike.guyona.exdepot.capability.StorageConfigProvider.STORAGE_CONFIG_CAPABILITY;
import static bike.guyona.exdepot.config.ExDepotConfig.keepConfigOnPickup;
import static bike.guyona.exdepot.gui.StorageConfigGuiHandler.STORAGE_CONFIG_GUI_ID;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.couldBeTileEntitySupported;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.isTileEntitySupported;

public class EventHandler {
    // COMMON VARS
    private static Map<Vec3i, byte[]> pickedUpStorageConfigCache;
    public static Map<String, Set<Integer>> modsAndCategoriesThatRegisterItems;

    // CLIENT VARS
    public static final int TICKS_PER_ITEM_FLIGHT = 3;
    private int ticksSinceLastItemFlown = 0;
    public ConcurrentLinkedDeque<Map<BlockPos, List<ItemStack>>> sortedItems;
    public List<SoundEvent> itemStoredSounds;
    public static final int STORE_ITEM_TONE_COUNT = 27;
    private int itemStoredCounter;

    public Map<String,Integer> guiContainerAccessOrders;

    // *****************
    // COMMON SUBS START
    // *****************
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
        if (event.getPlayer() instanceof ServerPlayerEntity && keepConfigOnPickup) {
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
        if (event.getHarvester() instanceof ServerPlayerEntity) {
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
        PlayerEntity player = event.getPlayer();
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
    // ***************
    // COMMON SUBS END
    // ***************


    // *****************
    // CLIENT SUBS START
    // *****************
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(KeyBindings.dumpItems.isPressed()){
            ExDepotMod.NETWORK.sendToServer(new StoreItemsMessage());
        }
    }

    public void addSortingResults(Map<BlockPos, List<ItemStack>> sortingResults) {
        sortedItems.add(sortingResults);
    }

    private void chooseSortedItemToFly() {
        while (!sortedItems.isEmpty() && sortedItems.getFirst().isEmpty()) {
            sortedItems.pollFirst();
            itemStoredCounter = 0;
        }
        if (sortedItems.isEmpty()) {
            return;
        }
        if (++ticksSinceLastItemFlown >= TICKS_PER_ITEM_FLIGHT) {
            ticksSinceLastItemFlown = 0;
        } else {
            return;
        }
        Map<BlockPos, List<ItemStack>> currentSort = sortedItems.getFirst();
        BlockPos currentPos = null;
        for (BlockPos pos : currentSort.keySet()) {
            currentPos = pos;
            break;
        }
        addFlyingItem(currentSort.get(currentPos).get(0), currentPos);
        playFlyingClickSound();
        currentSort.get(currentPos).remove(0);
        if (currentSort.get(currentPos).size() == 0) {
            currentSort.remove(currentPos);
        }
    }

    public void addFlyingItem(ItemStack stack, BlockPos target) {
        World worldIn = Minecraft.getInstance().world;
        PlayerEntity player = Minecraft.getInstance().player;
        player.getForward();
        Particle particle = new ParticleFlyingItem(
                worldIn,
                player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ(),
                player.getForward(),
                target.getX() + 0.5,target.getY() + 0.5,target.getZ() + 0.5,
                stack);

        Minecraft.getInstance().particles.addEffect(particle);
    }

    private void playFlyingClickSound() {
        Minecraft mc = Minecraft.getInstance();
        mc.world.playSound(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), this.itemStoredSounds.get(itemStoredCounter%STORE_ITEM_TONE_COUNT), SoundCategory.PLAYERS, 1, 1, false);
        itemStoredCounter++;
    }

    @SubscribeEvent
    public void onTick(@NotNull TickEvent.ClientTickEvent tick) {
        if(tick.phase == TickEvent.Phase.START) {
            chooseSortedItemToFly();
        }
    }
    // ***************
    // CLIENT SUBS END
    // ***************
}
