package bike.guyona.exdepot.items;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.sortingrules.SortingRuleProvider;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import static bike.guyona.exdepot.capabilities.DepotCapabilityProvider.DEPOT_CAPABILITY;

public class DepotConfiguratorWandItem extends Item {
    public DepotConfiguratorWandItem(Properties properties) {
        super(properties.stacksTo(1).tab(CreativeModeTab.TAB_TOOLS));
    }

    @Override
    @MethodsReturnNonnullByDefault
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(ctx.getClickedPos());
        ExDepotMod.LOGGER.info("You just clicked a {} on the {} side", blockEntity, level.isClientSide ? "client" : "server");
        // Remember to only add capabilities on the server, as that's where they're persisted.
        if (!level.isClientSide && blockEntity != null) {
            LazyOptional<IDepotCapability> depotCap = blockEntity.getCapability(DEPOT_CAPABILITY, Direction.UP);
            ExDepotMod.LOGGER.info("Capability is {}", depotCap.orElse(null));
            depotCap.ifPresent((IDepotCapability capability) -> {
                addModSortingRules(capability, blockEntity);
            });
        }
        return InteractionResult.CONSUME;
    }

    private void addModSortingRules(IDepotCapability depotCapability, BlockEntity depot) {
        LazyOptional<IItemHandler> lazyItemHandler = depot.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
        if (!lazyItemHandler.isPresent()) {
            ExDepotMod.LOGGER.error("Impossible: {} has Depot capability but not ItemHandler capability.", depot);
            return;
        }
        SortingRuleProvider ruleProvider = new SortingRuleProvider();
        IItemHandler itemHandler = lazyItemHandler.orElse(null);
        for (int i=0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            ModSortingRule rule = ruleProvider.getRule(stack, ModSortingRule.class);
            depotCapability.addRule(rule);
        }
        depot.setChanged();
    }
}
