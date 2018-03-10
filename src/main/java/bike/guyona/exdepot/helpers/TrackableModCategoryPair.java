package bike.guyona.exdepot.helpers;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.DISALLOWED_CATEGORIES;

public class TrackableModCategoryPair implements Comparable{
    public final String modId;
    public final String itemCategory;
    private ModContainer modCache;
    private CreativeTabs categoryCache;

    public TrackableModCategoryPair(String modId, String itemCategory) {
        this.modId = modId;
        this.itemCategory = itemCategory;
    }

    public TrackableModCategoryPair(ModContainer mod, CreativeTabs tab) {
        modId = mod.getModId();
        itemCategory = tab.getTabLabel();
        modCache = mod;
        categoryCache = tab;
    }

    @NotNull
    public ModContainer getMod() {
        if (modCache != null)
            return modCache;
        for (ModContainer mod: Loader.instance().getModList()) {
            if (mod.getModId().equals(modId)) {
                modCache = mod;
                break;
            }
        }
        if (modCache == null) {
            LOGGER.error("Mod {} is not registered. Deleting configuration.", modId); // TODO
        }
        return modCache;
    }

    @NotNull
    public CreativeTabs getCategory() {
        if (categoryCache != null)
            return categoryCache;
        for (CreativeTabs tab:CreativeTabs.CREATIVE_TAB_ARRAY) {
            if (!Arrays.asList(DISALLOWED_CATEGORIES).contains(tab) && tab.getTabLabel().equals(itemCategory)) {
                categoryCache = tab;
                break;
            }
        }
        if (categoryCache == null) {
            LOGGER.error("Category {} does not exist. Deleting configuration.", modId); // TODO
        }
        return categoryCache;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof TrackableModCategoryPair) {
            TrackableModCategoryPair t = (TrackableModCategoryPair) o;
            if (modId.equals(t.modId)) {
                return itemCategory.compareTo(t.itemCategory);
            }
            return modId.compareTo(t.modId);
        } else {
            throw new ClassCastException(String.format("object being compared is not a %s", this.getClass().toString()));
        }
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof TrackableModCategoryPair &&
                modId.equals(((TrackableModCategoryPair) obj).modId) &&
                itemCategory.equals(((TrackableModCategoryPair) obj).itemCategory));
    }

    @Override
    public int hashCode() {
        return modId.hashCode() + itemCategory.hashCode();
    }
}
