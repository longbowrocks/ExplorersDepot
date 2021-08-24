package bike.guyona.exdepot.gui;

import bike.guyona.exdepot.capability.StorageConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class MyContainerWrapper extends Container {
    public StorageConfig configForContainer;
    public BlockPos chestLocation;

    public MyContainerWrapper (@Nullable ContainerType<?> type, int windowId, PacketBuffer data) {
        this(type, windowId, data.readBlockPos(), StorageConfig.fromBytes(data.readByteArray()));
    }

    public MyContainerWrapper (@Nullable ContainerType<?> type, int windowId, BlockPos chestPos, StorageConfig conf) {
        super(type, windowId);
        chestLocation = chestPos;
        configForContainer = conf;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return false;
    }
}
