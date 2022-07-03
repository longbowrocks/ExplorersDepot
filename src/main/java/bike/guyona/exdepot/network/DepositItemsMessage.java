package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.helpers.DepotRouter;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.SortingRuleProvider;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static bike.guyona.exdepot.ExDepotMod.NETWORK_INSTANCE;
import static bike.guyona.exdepot.capabilities.DepotCapabilityProvider.DEPOT_CAPABILITY;

public class DepositItemsMessage {
    public void encode(FriendlyByteBuf buf) {

    }

    public static DepositItemsMessage decode(FriendlyByteBuf buf) {
        return new DepositItemsMessage();
    }

    public static void handle(DepositItemsMessage obj, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender == null) {
            ExDepotMod.LOGGER.warn("NO ONE sent a DepositItemsMessage");
        } else {
            ctx.get().enqueueWork(() -> {
                depositItems(sender);
                NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender), new DepositItemsResponse());
            });
        }
        ctx.get().setPacketHandled(true);
    }

    public static void depositItems(ServerPlayer player) {
        DepotRouter<ModSortingRule> modRouter = new DepotRouter<>();
        initializeRouters(player, getBlockEntityPositionsInRange(player), modRouter);
        Inventory inv = player.getInventory();
        for (int i=Inventory.getSelectionSize(); i<Inventory.INVENTORY_SIZE; i++) {
            ItemStack istack = inv.getItem(i);
            if (istack.isEmpty()) {
                continue;
            }
            ItemStack leftovers = applyRulesOfType(modRouter, istack, new SortingRuleProvider().getRule(istack, ModSortingRule.class));
            if (leftovers.getCount() != istack.getCount()) {
                inv.setItem(i, leftovers);
                inv.setChanged();
            }
        }
        player.displayClientMessage(new TextComponent("The message has arrived!"), false);
    }

    private static void initializeRouters(ServerPlayer player, List<BlockPos> positions, DepotRouter<ModSortingRule> modRouter) {
        for (BlockPos pos : positions) {
            BlockEntity block = player.level.getBlockEntity(pos);
            if (block != null) {
                ExDepotMod.LOGGER.info("Checking BlockEntity {} at {}...", block, block.getBlockPos());
                LazyOptional<IDepotCapability> lazyDepot = block.getCapability(DEPOT_CAPABILITY, Direction.UP);
                lazyDepot.ifPresent((depotCap) -> {
                    Set<ModSortingRule> rules = depotCap.getRules(ModSortingRule.class);
                    if (rules.size() > 0) {
                        modRouter.addRules(rules, block);
                    }
                    ExDepotMod.LOGGER.info("Found capability {} with {} mod sorting rules", depotCap, depotCap.getRules(ModSortingRule.class).size());
                });
            }
        }
    }

    private static List<BlockPos> getBlockEntityPositionsInRange(ServerPlayer player) {
        int rangeBlocks = 10;
        int rangeBlocksSquared = rangeBlocks * rangeBlocks;
        Vec3 pos = player.position();
        int xMin = SectionPos.posToSectionCoord(pos.x() - rangeBlocks);
        int xMax = SectionPos.posToSectionCoord(pos.x() + rangeBlocks);
        int zMin = SectionPos.posToSectionCoord(pos.z() - rangeBlocks);
        int zMax = SectionPos.posToSectionCoord(pos.z() + rangeBlocks);
        List<BlockPos> blocksInRange = new ArrayList<>();
        for (int x=xMin; x < xMax+1; x++) {
            for (int z=zMin; z < zMax+1; z++) {
                // Server will load chunks if they aren't already loaded, but range should be restricted in settings anyway.
                Set<BlockPos> blockPosInChunk = player.level.getChunk(x, z).getBlockEntitiesPos();
                blockPosInChunk.removeIf(blockPos -> blockPos.distSqr(player.blockPosition()) > rangeBlocksSquared);
                blocksInRange.addAll(blockPosInChunk);
            }
        }
        // This will ensure that if multiple rules of the same type match an item, chests will be preferred in this order:
        // lowest, westmost, northmost.
        blocksInRange.sort((BlockPos pos1, BlockPos pos2) -> {
            if (pos1.getY() != pos2.getY()) {
                return Integer.compare(pos1.getY(), pos2.getY());
            }else if (pos1.getX() != pos2.getX()) {
                return Integer.compare(pos1.getX(), pos2.getX());
            }else {
                return Integer.compare(pos1.getZ(), pos2.getZ());
            }
        });
        ExDepotMod.LOGGER.info("Found {} BlockEntities in range", blocksInRange.size());
        return blocksInRange;
    }

    @NotNull
    private static <V extends AbstractSortingRule> ItemStack applyRulesOfType(@NotNull DepotRouter<V> router, @NotNull ItemStack stack, @NotNull V matchableStack) {
        List<BlockEntity> matchingDepots = router.getDepots(matchableStack);
        for (BlockEntity depot : matchingDepots) {
            stack = transferStack(stack, depot);
        }
        return stack;
    }

    private static ItemStack transferStack(ItemStack stack, BlockEntity depot) {
        LazyOptional<IItemHandler> lazyItemHandler = depot.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
        if (!lazyItemHandler.isPresent()) {
            ExDepotMod.LOGGER.error("Impossible: I've been asked to sort into a {}, which somehow doesn't have ItemHandler capability.", depot);
            return stack;
        }
        IItemHandler itemHandler = lazyItemHandler.orElse(null);
        for (int i=0; i < itemHandler.getSlots(); i++) {
            // Pretty sure I don't need depot.setChanged(), because that's just for redstone updates.
            stack = itemHandler.insertItem(i, stack, false);
            if (stack.isEmpty()) {
                break;
            }
        }
        return stack;
    }
}
