package bike.guyona.exdepot.events;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.capabilities.DepotCapabilityProvider;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.helpers.ModSupportHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static bike.guyona.exdepot.ExDepotMod.*;
import static bike.guyona.exdepot.capabilities.DepotCapabilityProvider.DEPOT_CAPABILITY;


@Mod.EventBusSubscriber(modid = Ref.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class EventHandler {
    // playerID -> BlockPos -> depotCacheCompoundTag.
    // Technically key should include level, but one player can't leftclick a block in two levels in one tick.
    private static final Map<Integer, Map<BlockPos, CompoundTag>> pickedUpDepotCache = new HashMap<>();

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
        BlockEntity placedEntity = player.level.getBlockEntity(event.getPos());
        if (placedEntity == null) {
            LOGGER.error("Impossible: BlockPlace event has no placed block.");
            return;
        }
        // If this placed block is part of a BigDepot, copy depot from one of its siblings.
        boolean copiedFromSibling = false;
        for (BlockEntity e : ModSupportHelpers.getBigDepot(placedEntity)) {
            if (e != placedEntity) {
                if (copyDepot(e, placedEntity)) {
                    copiedFromSibling = true;
                }
                break;
            }
        }
        if (copiedFromSibling) {
            return;
        }
        // Otherwise try copying the cached capability.
        ItemStack placedItem = player.getItemInHand(player.getUsedItemHand());
        CompoundTag capabilityCache = placedItem.getTagElement(CAPABILITY_CACHE_KEY);
        if (capabilityCache == null) {
            return; // This item didn't have any capability info cached on it to restore.
        }
        placedEntity.getCapability(DepotCapabilityProvider.DEPOT_CAPABILITY).ifPresent((IDepotCapability capability) -> {
            capability.deserializeNBT(capabilityCache);
        });
    }

    public static CompoundTag getDepotCache(BlockPos pos, int playerId) {
        if (pickedUpDepotCache.containsKey(playerId)) {
            return pickedUpDepotCache.get(playerId).getOrDefault(pos, null);
        }
        return null;
    }

    // TODO this doesn't belong here
    private static boolean copyDepot(BlockEntity source, BlockEntity target) {
        LazyOptional<IDepotCapability> lazySourceCap = source.getCapability(DEPOT_CAPABILITY, Direction.UP);
        LazyOptional<IDepotCapability> lazyTargetCap = target.getCapability(DEPOT_CAPABILITY, Direction.UP);
        AtomicBoolean copied = new AtomicBoolean(false);
        lazySourceCap.ifPresent(sourceCap -> {
            lazyTargetCap.ifPresent(targetCap -> {
                targetCap.copyFrom(sourceCap);
                copied.set(true);
            });
        });
        return copied.get();
    }
}
