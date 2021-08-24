package bike.guyona.exdepot.network;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.helpers.GuiHelpers;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.item.ItemSortingRule;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.IItemHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.function.Supplier;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.getTileEntityFromBlockPos;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class StorageConfigCreateFromChestMessage{
    private BlockPos chestPos;

    public StorageConfigCreateFromChestMessage(){ this.chestPos = new BlockPos(-1,-1,-1); }

    public StorageConfigCreateFromChestMessage(BlockPos chestPos){ this.chestPos = chestPos; }

    public StorageConfigCreateFromChestMessage(PacketBuffer buf) {
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
        public static void onMessage(StorageConfigCreateFromChestMessage message, Supplier<NetworkEvent.Context> ctx) {
            // Associate chests with received StorageConfig, and add to cache.
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity serverPlayer = ctx.get().getSender();
                // Associate chest with received StorageConfig, and add to cache.
                TileEntity possibleChest = getTileEntityFromBlockPos(message.chestPos, serverPlayer.getServerWorld());
                if (possibleChest == null) {
                    GuiHelpers.openStorageConfigGui(serverPlayer, message.chestPos, new StorageConfig());
                    return;
                }
                IItemHandler itemHandler = possibleChest.getCapability(ITEM_HANDLER_CAPABILITY, Direction.UP).orElse(null);
                StorageConfig config = StorageConfig.fromTileEntity(possibleChest);
                StorageConfig storageConf = createConfFromChest(itemHandler, config);
                GuiHelpers.openStorageConfigGui(serverPlayer, message.chestPos, storageConf);
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

        // Create rules for all itemStacks that don't match an existing rule.
        for (ItemStack chestStack : chestStacks) {
            AbstractSortingRule rule = ExDepotMod.sortingRuleProvider.fromItemStack(chestStack, ItemSortingRule.class);
            if (rule == null) {
                LOGGER.error("Couldn't create rule {} for {}", ItemSortingRule.class, chestStack);
                continue;
            }
            config.addRule(rule);
        }
        return config;
    }
}
