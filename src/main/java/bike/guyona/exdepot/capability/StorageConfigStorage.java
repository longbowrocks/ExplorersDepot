package bike.guyona.exdepot.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

/**
 * Created by longb on 11/19/2017.
 * What a mouthful. Just remember that "StorageConfig" is the thing we're interested in. All other "Storage" in names
 * refers to other things.
 */
public class StorageConfigStorage implements Capability.IStorage<StorageConfig> {
    @Override
    public NBTBase writeNBT(Capability<StorageConfig> capability, StorageConfig instance, EnumFacing side) {
        return new NBTTagByteArray(instance.toBytes());
    }

    @Override
    public void readNBT(Capability<StorageConfig> capability, StorageConfig instance, EnumFacing side, NBTBase nbt) {
        if (nbt instanceof NBTTagByteArray){
            StorageConfig result = StorageConfig.fromBytes(((NBTTagByteArray)nbt).getByteArray());
            if (result == null) {
                return;
            }
            instance.copyFrom(result);
        } else {
            LOGGER.info("Why didn't I get a byte array back?");
        }
    }
}
