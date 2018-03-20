package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.ItemSortingRule;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.getContainerTileEntities;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class StorageConfigSmartCreateFromChestMessage implements IMessage, IMessageHandler<StorageConfigSmartCreateFromChestMessage, IMessage> {
    public StorageConfigSmartCreateFromChestMessage(){}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public IMessage onMessage(StorageConfigSmartCreateFromChestMessage message, MessageContext ctx) {
        // This is the player the packet was sent to the server from
        EntityPlayerMP serverPlayer = ctx.getServerHandler().playerEntity;
        serverPlayer.getServerWorld().addScheduledTask(() -> {
            List<TileEntity> chests = getContainerTileEntities(serverPlayer.openContainer);
            if (chests.size() == 0) {
                return;
            }
            // Associate chest with received StorageConfig, and add to cache.
            //noinspection SynchronizeOnNonFinalField
            synchronized (proxy) {
                IItemHandler itemHandler = chests.get(0).getCapability(ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                StorageConfig storageConf = createConfFromChest(itemHandler);
                ExDepotMod.NETWORK.sendTo(new StorageConfigCreateFromChestResponse(storageConf), serverPlayer);
            }
        });
        // No direct response packet
        return null;
    }

    private static StorageConfig createConfFromChest(IItemHandler itemHandler) {
        StorageConfig config = new StorageConfig();
        if (itemHandler == null) {
            LOGGER.error("This chest doesn't have an item handler, but it should");
            return config;
        }
        Map<Class<? extends AbstractSortingRule>, Set<AbstractSortingRule>> potentialRules = new HashMap<>();
        for (int chestInvIdx=0; chestInvIdx < itemHandler.getSlots(); chestInvIdx++) {
            ItemStack chestStack = itemHandler.getStackInSlot(chestInvIdx);
            if (!chestStack.isEmpty()) {
                for (Class<? extends AbstractSortingRule> ruleClass : proxy.sortingRuleProvider.ruleClasses) {
                    potentialRules.computeIfAbsent(ruleClass, k -> new HashSet<>());
                    potentialRules.get(ruleClass)
                            .add(proxy.sortingRuleProvider.fromItemStack(chestStack, ruleClass));
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
