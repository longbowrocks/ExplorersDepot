package bike.guyona.exdepot.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ExDepotConfig {
    public static Configuration configFile;

    public static final int storeRangeDefault=10;
    public static int storeRange = storeRangeDefault;
    private static String storageRangeName = "exdepot.config.storageRange";

    public static final boolean forceCompatibilityDefault = false;
    public static boolean forceCompatibility = forceCompatibilityDefault;
    private static String forceCompatibilityName = "exdepot.config.forceCompatibility";

    public static final boolean keepConfigOnPickupDefault = false;
    public static boolean keepConfigOnPickup = keepConfigOnPickupDefault;
    private static String keepConfigOnPickupName = "exdepot.config.keepConfigOnPickup";

    public static void syncConfig() {
        Property storeRangeProp = configFile.get(Configuration.CATEGORY_GENERAL, "storageRange", storeRangeDefault, "", 5, 50);
        storeRangeProp.setLanguageKey(storageRangeName);
        Property forceCompatProp = configFile.get(Configuration.CATEGORY_GENERAL, "forceCompatibility", forceCompatibilityDefault, "");
        forceCompatProp.setLanguageKey(forceCompatibilityName);
        forceCompatProp.setRequiresWorldRestart(true);
        Property keepConfigProp = configFile.get(Configuration.CATEGORY_GENERAL, "keepConfigOnPickup", keepConfigOnPickupDefault, "");
        keepConfigProp.setLanguageKey(keepConfigOnPickupName);

        configFile.getCategory(Configuration.CATEGORY_GENERAL).clear();

        configFile.getCategory(Configuration.CATEGORY_GENERAL).put(storeRangeProp.getName(), storeRangeProp);
        configFile.getCategory(Configuration.CATEGORY_GENERAL).put(forceCompatProp.getName(), forceCompatProp);
        configFile.getCategory(Configuration.CATEGORY_GENERAL).put(keepConfigProp.getName(), keepConfigProp);

        if(configFile.hasChanged())
            configFile.save();
    }
}
