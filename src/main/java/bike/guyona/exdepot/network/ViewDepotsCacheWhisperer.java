package bike.guyona.exdepot.network;

import bike.guyona.exdepot.client.particles.ViewDepotParticle;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
                    depotSummary.loc().getX(),
                    depotSummary.loc().getY(),
                    depotSummary.loc().getZ(),
                    depotSummary.modId(),
                    depotSummary.isSimpleDepot(),
                    depotSummary.chestFullness()
            );
            depotParticles.add(particle);
            minecraft.particleEngine.add(particle);
        }
    }

    private void sortSummaries(@NotNull List<ViewDepotSummary> depotSummaries) {
        depotSummaries.sort((ViewDepotSummary pos1, ViewDepotSummary pos2) -> {
            if (pos1.loc().getY() != pos2.loc().getY()) {
                return Integer.compare(pos1.loc().getY(), pos2.loc().getY());
            } else if (pos1.loc().getX() != pos2.loc().getX()) {
                return Integer.compare(pos1.loc().getX(), pos2.loc().getX());
            } else {
                return Integer.compare(pos1.loc().getZ(), pos2.loc().getZ());
            }
        });
    }
}
