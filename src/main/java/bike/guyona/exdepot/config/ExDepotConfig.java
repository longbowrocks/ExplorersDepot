package bike.guyona.exdepot.config;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.config.gui.ExtendedArrayEntry;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import static bike.guyona.exdepot.Ref.CATEGORY_MANUAL;

public class ExDepotConfig {
    public static Configuration configFile;

    private static final int storeRangeDefault=10;
    public static int storeRange = storeRangeDefault;
    private static String storageRangeName = "exdepot.config.storageRange";

    private static final boolean forceCompatibilityDefault = false;
    public static boolean forceCompatibility = forceCompatibilityDefault;
    private static String forceCompatibilityName = "exdepot.config.forceCompatibility";

    private static final boolean keepConfigOnPickupDefault = false;
    public static boolean keepConfigOnPickup = keepConfigOnPickupDefault;
    private static String keepConfigOnPickupName = "exdepot.config.keepConfigOnPickup";

    private static final String compatibilityModeDefault = Ref.COMPAT_MODE_VANILLA;
    public static String compatibilityMode = compatibilityModeDefault;
    private static String compatibilityModeName = "exdepot.config.compatibilityMode";

    private static final String compatListTypeDefault = Ref.COMPAT_MAN_TYPE_BLACK;
    public static String compatListType = compatListTypeDefault;
    private static String compatListTypeName = "exdepot.config.compatListType";

    private static final String[] compatListDefault = new String[]{};
    public static String[] compatList = compatListDefault;
    private static String compatListName = "exdepot.config.compatList";

    public static void syncConfig() {
        Property storeRangeProp = configFile.get(Configuration.CATEGORY_GENERAL, "storageRange", storeRangeDefault, "", 5, 50);
        storeRangeProp.setLanguageKey(storageRangeName);
        Property forceCompatProp = configFile.get(Configuration.CATEGORY_GENERAL, "forceCompatibility", forceCompatibilityDefault);
        forceCompatProp.setLanguageKey(forceCompatibilityName);
        forceCompatProp.setRequiresWorldRestart(true);
        Property keepConfigProp = configFile.get(Configuration.CATEGORY_GENERAL, "keepConfigOnPickup", keepConfigOnPickupDefault);
        keepConfigProp.setLanguageKey(keepConfigOnPickupName);
        Property compatModeProp = configFile.get(Configuration.CATEGORY_GENERAL, "compatibilityMode", compatibilityModeDefault, "", new String[]{Ref.COMPAT_MODE_VANILLA, Ref.COMPAT_MODE_DISCOVER, Ref.COMPAT_MODE_MANUAL});
        compatModeProp.setLanguageKey(compatibilityModeName);
        Property compatListTypeProp = configFile.get(CATEGORY_MANUAL, "compatibilityListType", compatListTypeDefault, "", new String[]{Ref.COMPAT_MAN_TYPE_WHITE, Ref.COMPAT_MAN_TYPE_BLACK});
        compatListTypeProp.setLanguageKey(compatListTypeName);
        Property compatListProp = configFile.get(CATEGORY_MANUAL, "compatibilityList", compatListDefault);
        compatListProp.setLanguageKey(compatListName);
        compatListProp.setConfigEntryClass(ExtendedArrayEntry.class);

        storeRange = storeRangeProp.getInt();
        forceCompatibility = forceCompatProp.getBoolean();
        keepConfigOnPickup = keepConfigProp.getBoolean();
        compatibilityMode = compatModeProp.getString();
        compatListType = compatListTypeProp.getString();
        compatList = compatListProp.getStringList();

        configFile.getCategory(Configuration.CATEGORY_GENERAL).clear();
        configFile.getCategory(CATEGORY_MANUAL).clear();

        configFile.getCategory(Configuration.CATEGORY_GENERAL).put(storeRangeProp.getName(), storeRangeProp);
        configFile.getCategory(Configuration.CATEGORY_GENERAL).put(forceCompatProp.getName(), forceCompatProp);
        configFile.getCategory(Configuration.CATEGORY_GENERAL).put(keepConfigProp.getName(), keepConfigProp);
        configFile.getCategory(Configuration.CATEGORY_GENERAL).put(compatModeProp.getName(), compatModeProp);
        configFile.getCategory(CATEGORY_MANUAL).put(compatListTypeProp.getName(), compatListTypeProp);
        configFile.getCategory(CATEGORY_MANUAL).put(compatListProp.getName(), compatListProp);

        if(configFile.hasChanged())
            configFile.save();
    }
}
