package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.config.ExDepotConfig;
import bike.guyona.exdepot.helpers.ChestFullness;
import bike.guyona.exdepot.helpers.DepotRouter;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static bike.guyona.exdepot.ExDepotMod.NETWORK_INSTANCE;
import static bike.guyona.exdepot.capabilities.DepotCapabilityProvider.DEPOT_CAPABILITY;
import static net.minecraft.client.renderer.LevelRenderer.CHUNK_SIZE;

public class ViewDepotsMessage {
    public void encode(FriendlyByteBuf buf) {}

    public static ViewDepotsMessage decode(FriendlyByteBuf buf) {
        return new ViewDepotsMessage();
    }

    public static void handle(ViewDepotsMessage obj, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender == null) {
            ExDepotMod.LOGGER.warn("NO ONE sent a ViewDepotsMessage");
        } else {
            ctx.get().enqueueWork(() -> {
                Vector<BlockEntity> nearbyChests = getLocalChests(sender);
                AtomicReference<IDepotCapability> nearestConfigWrapped = new AtomicReference<>();
                if (nearbyChests.size() > 0) {
                    LazyOptional<IDepotCapability> lazyDepot = nearbyChests.get(0).getCapability(DEPOT_CAPABILITY, Direction.UP);
                    lazyDepot.ifPresent(nearestConfigWrapped::set);
                }
                if (nearestConfigWrapped.get() == null) {
                    NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender), new ViewDepotsResponse(null, null, false, ChestFullness.EMPTY));
                } else {
                    BlockEntity nearestChest = nearbyChests.get(0);
                    Set<ModSortingRule> modRules = nearestConfigWrapped.get().getRules(ModSortingRule.class);
                    Optional<String> modIdOptional = modRules.stream().map(ModSortingRule::getModId).findFirst();
                    String modId = modIdOptional.orElse(null);
                    boolean simpleDepot = modRules.size() == 1 && nearestConfigWrapped.get().size() == 1;
                    ChestFullness chestFullness = getChestFullness(nearestChest);
                    NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender), new ViewDepotsResponse(nearestChest.getBlockPos(), modId, simpleDepot, chestFullness));
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }

    private static Vector<BlockEntity> getLocalChests(ServerPlayer player){
        Vector<BlockEntity> chests = new Vector<>();
        int chunkDist = (ExDepotConfig.storeRange.get() / CHUNK_SIZE) + 1;
        ExDepotMod.LOGGER.info("Storage range is {} blocks, or {} chunks", ExDepotConfig.storeRange.get(), chunkDist);
        for (int chunkX = player.chunkPosition().x-chunkDist; chunkX <= player.chunkPosition().x+chunkDist; chunkX++) {
            for (int chunkZ = player.chunkPosition().z-chunkDist; chunkZ <= player.chunkPosition().z+chunkDist; chunkZ++) {
                Collection<BlockEntity> entities = player.level.getChunk(chunkX, chunkZ).getBlockEntities().values();
                for (BlockEntity entity:entities) {
                    LazyOptional<IDepotCapability> lazyDepot = entity.getCapability(DEPOT_CAPABILITY, Direction.UP);
                    lazyDepot.ifPresent((depotCap) -> {
                        BlockPos chestPos = entity.getBlockPos();
                        if (player.position().distanceToSqr(chestPos.getX(), chestPos.getY(), chestPos.getZ()) < ExDepotConfig.storeRange.get()*ExDepotConfig.storeRange.get()) {
                            chests.add(entity);
                        }
                    });
                }
            }
        }
        return chests;
    }

    /**
     * @param chest
     * @return fullness. A chest can have room (0), have 80% slots full (1), or have 100% slots full (2)
     */
    private static ChestFullness getChestFullness(BlockEntity chest) {
        LazyOptional<IItemHandler> lazyItemHandler = chest.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
        if (!lazyItemHandler.isPresent()) {
            return ChestFullness.EMPTY;
        }
        IItemHandler itemHandler = lazyItemHandler.orElse(null);
        int filledSlots = 0;
        for (int i=0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty())
                filledSlots += 1;
        }
        float pctFull = (float) filledSlots / (float) itemHandler.getSlots();
        if (pctFull < 0.8) {
            return ChestFullness.EMPTY;
        } else if (pctFull < 1) {
            return ChestFullness.FILLING;
        }
        return ChestFullness.FULL;
    }
}
