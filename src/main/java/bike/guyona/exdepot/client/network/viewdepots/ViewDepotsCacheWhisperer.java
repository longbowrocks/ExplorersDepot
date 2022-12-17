package bike.guyona.exdepot.client.network.viewdepots;

import bike.guyona.exdepot.client.particles.ViewDepotParticle;
import bike.guyona.exdepot.network.viewdepots.ViewDepotSummary;
import bike.guyona.exdepot.network.viewdepots.ViewDepotsMessage;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static bike.guyona.exdepot.ExDepotMod.NETWORK_INSTANCE;

public class ViewDepotsCacheWhisperer {
    public static final int VIEW_DEPOTS_CACHE_REFRESH_INTERVAL_MS = 1000;
    private static long lastUpdatedViewableConfigs = 0;
    private List<ViewDepotSummary> depotSummaries = new ArrayList<>();
    private final List<ViewDepotParticle> depotParticles = new ArrayList<>();

    public boolean isUpdateDue() {
        return System.currentTimeMillis() > lastUpdatedViewableConfigs + VIEW_DEPOTS_CACHE_REFRESH_INTERVAL_MS;
    }

    public void setUpdated() {
        lastUpdatedViewableConfigs = System.currentTimeMillis();
    }

    // Clients know they want to ping occasionally for updates.
    public void triggerUpdateFromClient() {
        NETWORK_INSTANCE.sendToServer(new ViewDepotsMessage());
        this.setUpdated();
    }

    public boolean isActive() {
        return depotSummaries.size() > 0;
    }

    public boolean areSummariesChanged(@NotNull List<ViewDepotSummary> depotSummaries) {
        if (this.depotSummaries.size() != depotSummaries.size()) {
            return true;
        }
        sortSummaries(depotSummaries);
        for (int i=0; i<depotSummaries.size(); i++) {
            if (!this.depotSummaries.get(i).equals(depotSummaries.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void resetParticleLifetimes() {
        for (ViewDepotParticle particle : depotParticles) {
            particle.resetAge();
        }
    }

    public void replaceParticles(@NotNull List<ViewDepotSummary> depotSummaries) {
        // Out with the old
        for (ViewDepotParticle particle : depotParticles) {
            particle.remove();
        }
        depotParticles.clear();
        this.depotSummaries.clear();
        // In with the new
        Minecraft minecraft = Minecraft.getInstance();
        this.depotSummaries = depotSummaries;
        sortSummaries(this.depotSummaries);
        for (ViewDepotSummary depotSummary : this.depotSummaries) {
            ViewDepotParticle particle = new ViewDepotParticle(
                    minecraft.level,
                    depotSummary.loc().x,
                    depotSummary.loc().y,
                    depotSummary.loc().z,
                    depotSummary.modId(),
                    depotSummary.isSimpleDepot(),
                    depotSummary.chestFullness()
            );
            depotParticles.add(particle);
            minecraft.particleEngine.add(particle);
        }
        // Just in case server triggered the update, thereby bypassing the typical procedure.
        this.setUpdated();
    }

    private void sortSummaries(@NotNull List<ViewDepotSummary> depotSummaries) {
        depotSummaries.sort((ViewDepotSummary pos1, ViewDepotSummary pos2) -> {
            if (pos1.loc().y != pos2.loc().y) {
                return Double.compare(pos1.loc().y, pos2.loc().y);
            } else if (pos1.loc().x != pos2.loc().x) {
                return Double.compare(pos1.loc().x, pos2.loc().x);
            } else {
                return Double.compare(pos1.loc().z, pos2.loc().z);
            }
        });
    }
}
