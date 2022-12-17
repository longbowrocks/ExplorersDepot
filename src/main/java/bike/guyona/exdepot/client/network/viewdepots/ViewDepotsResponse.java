package bike.guyona.exdepot.client.network.viewdepots;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.client.events.EventHandler;
import bike.guyona.exdepot.network.viewdepots.ViewDepotSummary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;

public class ViewDepotsResponse {
    public static void updateViewDepotsCache(List<ViewDepotSummary> depotSummaries) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            ExDepotMod.LOGGER.error("Impossible: the client doesn't have a player");
            return;
        }
        if (EventHandler.VIEW_DEPOTS_CACHE_WHISPERER.areSummariesChanged(depotSummaries)) {
            EventHandler.VIEW_DEPOTS_CACHE_WHISPERER.replaceParticles(depotSummaries);
        } else {
            EventHandler.VIEW_DEPOTS_CACHE_WHISPERER.resetParticleLifetimes();
        }
    }
}
