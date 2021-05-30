package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.IItemHandler;

import java.util.*;
import java.util.function.Supplier;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.getTileEntityFromBlockPos;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class StorageConfigSmartCreateFromChestMessage {
    private BlockPos chestPos;

    public StorageConfigSmartCreateFromChestMessage(){}

    public StorageConfigSmartCreateFromChestMessage(BlockPos chestPos){ this.chestPos = chestPos; }

    public StorageConfigSmartCreateFromChestMessage(PacketBuffer buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        chestPos = new BlockPos(x, y, z);
        LOGGER.info("Received {}", chestPos);
    }

    public void encode(PacketBuffer buf) {
        LOGGER.info("Sending over {}", chestPos);
        buf.writeInt(chestPos.getX());
        buf.writeInt(chestPos.getY());
        buf.writeInt(chestPos.getZ());
    }

    public static class Handler {
        public static void onMessage(StorageConfigSmartCreateFromChestMessage message, Supplier<NetworkEvent.Context> ctx) {
            // Associate chests with received StorageConfig, and add to cache.
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity serverPlayer = ctx.get().getSender();
                TileEntity possibleChest = getTileEntityFromBlockPos(message.chestPos, serverPlayer.getServerWorld());
                if (possibleChest == null) {
                    ExDepotMod.NETWORK.send(
                            PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new StorageConfigCreateFromChestResponse(new StorageConfig(), message.chestPos)
                    );
                    return;
                }
                IItemHandler itemHandler = possibleChest.getCapability(ITEM_HANDLER_CAPABILITY, Direction.UP).orElse(null);
                StorageConfig config = StorageConfig.fromTileEntity(possibleChest);
                StorageConfig storageConf = createConfFromChest(itemHandler, config);
                ExDepotMod.NETWORK.send(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new StorageConfigCreateFromChestResponse(storageConf, message.chestPos)
                );
            });
            ctx.get().setPacketHandled(true);
        }
    }

    private static StorageConfig createConfFromChest(IItemHandler itemHandler, StorageConfig config) {
        if (config == null) {
            config = new StorageConfig();
        }
        if (itemHandler == null) {
            LOGGER.error("This chest doesn't have an item handler, but it should");
            return config;
        }
        // Get hashset of existing rules.
        Set<AbstractSortingRule> existingRules = new HashSet<>();
        for (Class<? extends AbstractSortingRule> ruleClass : ExDepotMod.sortingRuleProvider.ruleClasses) {
            Set<? extends AbstractSortingRule> existingRulesOfClass = config.getRules(ruleClass);
            if (existingRulesOfClass == null){
                continue;
            }
            existingRules.addAll(existingRulesOfClass);
        }

        // Get all itemStacks that don't match an existing rule.
        Vector<ItemStack> chestStacks = new Vector<>();
        for (int chestInvIdx=0; chestInvIdx < itemHandler.getSlots(); chestInvIdx++) {
            ItemStack chestStack = itemHandler.getStackInSlot(chestInvIdx);
            if (!chestStack.isEmpty()) {
                boolean matches = false;
                for (Class<? extends AbstractSortingRule> ruleClass : ExDepotMod.sortingRuleProvider.ruleClasses) {
                    AbstractSortingRule rule = ExDepotMod.sortingRuleProvider.fromItemStack(chestStack, ruleClass);
                    if (rule == null) {
                        LOGGER.error("Couldn't create rule {} for {}", ruleClass, chestStack);
                        continue;
                    }
                    if (existingRules.contains(rule)){
                        matches = true;
                    }
                }
                if (!matches) {
                    chestStacks.add(chestStack);
                }
            }
        }

        // Create potential rules for all itemStacks that don't match an existing rule.
        Map<Class<? extends AbstractSortingRule>, Set<AbstractSortingRule>> potentialRules = new HashMap<>();
        for (ItemStack chestStack : chestStacks) {
            if (!chestStack.isEmpty()) {
                for (Class<? extends AbstractSortingRule> ruleClass : ExDepotMod.sortingRuleProvider.ruleClasses) {
                    AbstractSortingRule rule = ExDepotMod.sortingRuleProvider.fromItemStack(chestStack, ruleClass);
                    if (rule == null) {
                        LOGGER.error("Couldn't create rule {} for {}", ruleClass, chestStack);
                        continue;
                    }
                    potentialRules.computeIfAbsent(ruleClass, k -> new HashSet<>());
                    potentialRules.get(ruleClass).add(rule);
                }
            }
        }
        int minRulesSize = Integer.MAX_VALUE;
        Set<AbstractSortingRule> rules = null;
        // Find the smallest rule set required to represent chest contents. Most specific rule set wins a tie.
        for (Class ruleClass : ExDepotMod.sortingRuleProvider.ruleClasses) {
            if (potentialRules.get(ruleClass) != null) {
                int rulesSize = potentialRules.get(ruleClass).size();
                if (rulesSize < minRulesSize) {
                    minRulesSize = rulesSize;
                    rules = potentialRules.get(ruleClass);
                }
            }
        }
        if (rules != null) {
            for (AbstractSortingRule rule : rules) {
                config.addRule(rule);
            }
        }
        return config;
    }
}
