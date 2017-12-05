package bike.guyona.exdepot.storageconfig.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by longb on 11/19/2017.
 */
public class StorageConfigProvider implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(StorageConfig.class)
    public static final Capability<StorageConfig> STORAGE_CONFIG_CAPABILITY = null;

    private StorageConfig instance = STORAGE_CONFIG_CAPABILITY.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == STORAGE_CONFIG_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == STORAGE_CONFIG_CAPABILITY ? STORAGE_CONFIG_CAPABILITY.<T> cast(this.instance) : null;
    }

    @Override
    public NBTBase serializeNBT() {
        return STORAGE_CONFIG_CAPABILITY.getStorage().writeNBT(STORAGE_CONFIG_CAPABILITY, this.instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        STORAGE_CONFIG_CAPABILITY.getStorage().readNBT(STORAGE_CONFIG_CAPABILITY, this.instance, null, nbt);
    }
}
