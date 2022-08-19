package bike.guyona.exdepot.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

public class DepotCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    /**
     * To understand capabilities, some language must be defined.
     *
     * Capability:
     * An interface that defines methods and state for what your capability does.
     * Capability Implementation:
     * A class that exposes methods and maintains state for what your capability does.
     * CapabilityProvider:
     * A class that can get the correct capability implementation for a capability.
     * Typically, the provider is the item/block to which the capability is attached.
     */
    public static final Capability<IDepotCapability> DEPOT_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private final LazyOptional<IDepotCapability> instance = LazyOptional.of(DefaultDepotCapability::new);

    public IDepotCapability fromBlockEntity(BlockEntity blockEntity) {
        LazyOptional<IDepotCapability> lazyPossibleCapability = blockEntity.getCapability(DEPOT_CAPABILITY, Direction.UP);
        Optional<IDepotCapability> possibleCapability = lazyPossibleCapability.resolve();
        if (possibleCapability.isEmpty()) {
            LOGGER.warn("Called fromBlockEntity on a block that doesn't have my capability.");
        }
        return possibleCapability.orElse(null);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == DEPOT_CAPABILITY ? instance.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.orElse(new DefaultDepotCapability()).serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.ifPresent((cap) -> {
            cap.deserializeNBT(nbt);
        });
    }
}
