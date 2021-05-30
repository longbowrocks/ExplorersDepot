package bike.guyona.exdepot.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by longb on 11/19/2017.
 */
public class StorageConfigProvider implements ICapabilitySerializable<INBT> {
    @CapabilityInject(StorageConfig.class)
    public static final Capability<StorageConfig> STORAGE_CONFIG_CAPABILITY = null;

    // TODO: this smells, but between the ambiguity of capability terms and the awkwardness of using the Optional interface here, I can't tell why.
    private StorageConfig instance = STORAGE_CONFIG_CAPABILITY.getDefaultInstance();
    private LazyOptional<StorageConfig> instanceCache = LazyOptional.of(() -> instance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == STORAGE_CONFIG_CAPABILITY) {
            return (LazyOptional<T>) instanceCache;
        }
        return LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        return STORAGE_CONFIG_CAPABILITY.getStorage().writeNBT(STORAGE_CONFIG_CAPABILITY, this.instance, null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        STORAGE_CONFIG_CAPABILITY.getStorage().readNBT(STORAGE_CONFIG_CAPABILITY, this.instance, null, nbt);
    }
}
