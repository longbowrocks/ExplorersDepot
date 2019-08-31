package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.SortingRuleMatcher;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.getContainerTileEntities;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.getTileEntityFromBlockPos;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class StorageConfigSmartCreateFromChestMessage implements IMessage, IMessageHandler<StorageConfigSmartCreateFromChestMessage, IMessage> {
    private BlockPos chestPos;

    public StorageConfigSmartCreateFromChestMessage(){}

    public StorageConfigSmartCreateFromChestMessage(BlockPos chestPos){ this.chestPos = chestPos; }

    @Override
    public void toBytes(ByteBuf buf) {
        LOGGER.info("Sending over {}", chestPos);
        buf.writeInt(chestPos.getX());
        buf.writeInt(chestPos.getY());
        buf.writeInt(chestPos.getZ());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        chestPos = new BlockPos(x, y, z);
        LOGGER.info("Received {}", chestPos);
    }

    @Override
    public IMessage onMessage(StorageConfigSmartCreateFromChestMessage message, MessageContext ctx) {
        // This is the player the packet was sent to the server from
        EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
        serverPlayer.getServerWorld().addScheduledTask(() -> {
            // Associate chest with received StorageConfig, and add to cache.
            //noinspection SynchronizeOnNonFinalField
            synchronized (proxy) {
                TileEntity possibleChest = getTileEntityFromBlockPos(message.chestPos, serverPlayer.getServerWorld());
                if (possibleChest == null) {
                    ExDepotMod.NETWORK.sendTo(new StorageConfigCreateFromChestResponse(new StorageConfig(), message.chestPos), serverPlayer);
                    return;
                }
                IItemHandler itemHandler = possibleChest.getCapability(ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                StorageConfig config = StorageConfig.fromTileEntity(possibleChest);
                StorageConfig storageConf = createConfFromChest(itemHandler, config);
                ExDepotMod.NETWORK.sendTo(new StorageConfigCreateFromChestResponse(storageConf, message.chestPos), serverPlayer);
            }
        });
        // No direct response packet
        return null;
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
        for (Class<? extends AbstractSortingRule> ruleClass : proxy.sortingRuleProvider.ruleClasses) {
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
                for (Class<? extends AbstractSortingRule> ruleClass : proxy.sortingRuleProvider.ruleClasses) {
                    AbstractSortingRule rule = proxy.sortingRuleProvider.fromItemStack(chestStack, ruleClass);
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
                for (Class<? extends AbstractSortingRule> ruleClass : proxy.sortingRuleProvider.ruleClasses) {
                    AbstractSortingRule rule = proxy.sortingRuleProvider.fromItemStack(chestStack, ruleClass);
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
        for (Class ruleClass : proxy.sortingRuleProvider.ruleClasses) {
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
