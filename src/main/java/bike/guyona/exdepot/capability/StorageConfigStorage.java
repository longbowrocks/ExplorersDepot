package bike.guyona.exdepot.capability;

import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

/**
 * Created by longb on 11/19/2017.
 * What a mouthful. Just remember that "StorageConfig" is the thing we're interested in. All other "Storage" in names
 * refers to other things.
 */
public class StorageConfigStorage implements Capability.IStorage<StorageConfig> {
    @Override
    public INBT writeNBT(Capability<StorageConfig> capability, StorageConfig instance, Direction side) {
        return new ByteArrayNBT(instance.toBytes());
    }

    @Override
    public void readNBT(Capability<StorageConfig> capability, StorageConfig instance, Direction side, INBT nbt) {
        if (nbt instanceof ByteArrayNBT){
            StorageConfig result = StorageConfig.fromBytes(((ByteArrayNBT)nbt).getByteArray());
            if (result == null) {
                return;
            }
            instance.copyFrom(result);
        } else {
            LOGGER.error("Why didn't I get a byte array back?");
        }
    }
}
