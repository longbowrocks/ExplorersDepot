package bike.guyona.exdepot.events;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.capabilities.DepotCapabilityProvider;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.client.DepositItemsJuice;
import bike.guyona.exdepot.network.ViewDepotsMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

import static bike.guyona.exdepot.ExDepotMod.*;


@Mod.EventBusSubscriber(modid = Ref.MODID, value = Dist.CLIENT)
public class EventHandler {
    public static final DepositItemsJuice JUICER = new DepositItemsJuice();
    private static long lastUpdatedViewableConfigs = 0;
    // playerID -> BlockPos -> depotCacheCompoundTag.
    // Technically key should include level, but one player can't leftclick a block in two levels in one tick.
    private static final Map<Integer, Map<BlockPos, CompoundTag>> pickedUpDepotCache = new HashMap<>();
    private static final int VIEWABLE_CONFIG_REFRESH_INTERVAL_MS = 1000;

    @SubscribeEvent
    static void onClientTick(TickEvent.ClientTickEvent event) {
        JUICER.handleClientTick();
        long curTime = System.currentTimeMillis();
        if (isIngame() && curTime > lastUpdatedViewableConfigs + VIEWABLE_CONFIG_REFRESH_INTERVAL_MS) {
            NETWORK_INSTANCE.sendToServer(new ViewDepotsMessage());
            lastUpdatedViewableConfigs = curTime;
        }
    }

    //Clear DepotCap from previous tick.
    @SubscribeEvent
    static void onServerTick(TickEvent.ServerTickEvent event) {
        if (pickedUpDepotCache.size() > 0) {
            pickedUpDepotCache.clear();
            LOGGER.debug("Cleared out depot cache on phase: {}", event.phase);
        }
    }

    //Read DepotCap from BlockEntity, so bike.guyona.exdepot.loot.DepotPickerUpperLootModifier can cache it in item NBT.
    @SubscribeEvent
    static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        BlockEntity harvestableEntity = player.level.getBlockEntity(event.getPos());
        if (harvestableEntity == null) {
            return;
        }
        harvestableEntity.getCapability(DepotCapabilityProvider.DEPOT_CAPABILITY).ifPresent(cap -> {
            if (cap.isEmpty()) {
                return;
            }
            pickedUpDepotCache.computeIfAbsent(player.getId(), (playerId) -> new HashMap<>()).put(event.getPos(), cap.serializeNBT());
            LOGGER.debug("{} that was clicked had {}", harvestableEntity, cap);
        });
    }

    //Read DepotCap from NBT, back into BlockEntity.
    @SubscribeEvent
    static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getPlacedBlock().hasBlockEntity()) {
            return;
        }
        ItemStack placedItem = player.getItemInHand(player.getUsedItemHand());
        CompoundTag capabilityCache = placedItem.getTagElement(CAPABILITY_CACHE_KEY);
        if (capabilityCache == null) {
            return; // This item didn't have any capability info cached on it to restore.
        }
        BlockEntity placedEntity = player.level.getBlockEntity(event.getPos());
        if (placedEntity == null) {
            LOGGER.error("Impossible: BlockPlace event has no placed block.");
            return;
        }
        placedEntity.getCapability(DepotCapabilityProvider.DEPOT_CAPABILITY).ifPresent((IDepotCapability capability) -> {
            capability.deserializeNBT(capabilityCache);
        });;
    }

    private static boolean isIngame() {
        return Minecraft.getInstance().level != null;
    }

    public static CompoundTag getDepotCache(BlockPos pos, int playerId) {
        if (pickedUpDepotCache.containsKey(playerId)) {
            return pickedUpDepotCache.get(playerId).getOrDefault(pos, null);
        }
        return null;
    }
}
