package bike.guyona.exdepot.client.network.deposititems;

import bike.guyona.exdepot.ExDepotMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

import static bike.guyona.exdepot.client.events.EventHandler.JUICER;

public class DepositItemsResponse {
    public static void queueDepositJuice(Map<BlockPos, List<ItemStack>> sortingResults) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            ExDepotMod.LOGGER.error("Impossible: the client doesn't have a player");
            return;
        }
        JUICER.enqueueDepositEvent(sortingResults);
    }
}
