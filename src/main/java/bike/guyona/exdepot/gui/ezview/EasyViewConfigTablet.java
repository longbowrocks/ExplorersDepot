package bike.guyona.exdepot.gui.ezview;

import bike.guyona.exdepot.capability.StorageConfig;
import net.minecraft.util.math.BlockPos;

public class EasyViewConfigTablet {
    // If you have the wand, these are re-initialized every few seconds. Might be better to do that differently,
    // but few seconds is fine for now.
    private StorageConfig storageConfig;
    private BlockPos storageConfigLocation;

    // if this gets a StorageConfig, it persists that StorageConfig. It re-renders every frame I guess.
    public EasyViewConfigTablet(StorageConfig storageConfig, BlockPos storageConfigLocation) {
        this.storageConfig = storageConfig;
        this.storageConfigLocation = storageConfigLocation;
    }

    public void render() {
        System.out.println("Yeah that's the deal.");
    }
}
