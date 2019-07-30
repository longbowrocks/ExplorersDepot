package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.SortingRuleMatcher;
import bike.guyona.exdepot.sortingrules.item.ItemSortingRule;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.IItemHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.getContainerTileEntities;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class StorageConfigCreateFromChestMessage implements IMessage, IMessageHandler<StorageConfigCreateFromChestMessage, IMessage> {
    public StorageConfigCreateFromChestMessage(){}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public IMessage onMessage(StorageConfigCreateFromChestMessage message, MessageContext ctx) {
        // This is the player the packet was sent to the server from
        EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
        serverPlayer.getServerWorld().addScheduledTask(() -> {
            List<TileEntity> chests = getContainerTileEntities(serverPlayer.openContainer);
            if (chests.size() == 0) {
                return;
            }
            // Associate chest with received StorageConfig, and add to cache.
            //noinspection SynchronizeOnNonFinalField
            synchronized (proxy) {
                IItemHandler itemHandler = chests.get(0).getCapability(ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                StorageConfig config = StorageConfig.fromContainer(serverPlayer.openContainer);
                StorageConfig storageConf = createConfFromChest(itemHandler, config);
                ExDepotMod.NETWORK.sendTo(new StorageConfigCreateFromChestResponse(storageConf), serverPlayer);
            }
        });
        // No direct response packet
        return null;
    }

    private static StorageConfig createConfFromChest(IItemHandler itemHandler, StorageConfig config) {
        if (itemHandler == null) {
            LOGGER.error("This chest doesn't have an item handler, but it should");
            return config;
        }
        if (config == null) {
            config = new StorageConfig();
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

        // Create rules for all itemStacks that don't match an existing rule.
        for (ItemStack chestStack : chestStacks) {
            AbstractSortingRule rule = proxy.sortingRuleProvider.fromItemStack(chestStack, ItemSortingRule.class);
            if (rule == null) {
                LOGGER.error("Couldn't create rule {} for {}", ItemSortingRule.class, chestStack);
                continue;
            }
            config.addRule(rule);
        }
        return config;
    }
}
