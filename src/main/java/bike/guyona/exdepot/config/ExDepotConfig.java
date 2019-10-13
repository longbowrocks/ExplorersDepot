package bike.guyona.exdepot.config;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.config.gui.CustomValidationArrayEntry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.*;

import static bike.guyona.exdepot.Ref.CATEGORY_MANUAL;

public class ExDepotConfig {
    public static Configuration configFile;

    private static final int storeRangeDefault=10;
    public static int storeRange = storeRangeDefault;
    private static String storageRangeLangKey = "exdepot.config.storageRange";

    private static final boolean forceCompatibilityDefault = false;
    @Deprecated
    public static boolean forceCompatibility = forceCompatibilityDefault;
    private static String forceCompatibilityLangKey = "exdepot.config.forceCompatibility";

    private static final boolean keepConfigOnPickupDefault = false;
    public static boolean keepConfigOnPickup = keepConfigOnPickupDefault;
    private static String keepConfigOnPickupLangKey = "exdepot.config.keepConfigOnPickup";

    private static final String compatibilityModeDefault = Ref.COMPAT_MODE_VANILLA;
    @Deprecated
    public static String compatibilityMode = compatibilityModeDefault;
    private static String compatibilityModeLangKey = "exdepot.config.compatibilityMode";

    private static final String compatListTypeDefault = Ref.MANUAL_COMPAT_TYPE_BLACK;
    @Deprecated
    public static String compatListType = compatListTypeDefault;
    private static String compatListTypeLangKey = "exdepot.config.compatListType";

    private static final String[] compatListDefault = new String[]{};
    @Deprecated
    public static String[] compatList = compatListDefault;
    private static String compatListLangKey = "exdepot.config.compatList";

    private static final boolean compatListIngameConfDefault = false;
    @Deprecated
    public static boolean compatListIngameConf = compatListIngameConfDefault;
    private static String compatListIngameConfLangKey = "exdepot.config.compatListIngameConf";


    private static Set<String> compatListFqClassnamesCache;
    private static List<String> compatListClassnamesMatchesCache;

    public static void syncConfig() {
        Property storeRangeProp = configFile.get(Configuration.CATEGORY_GENERAL, "storageRange", storeRangeDefault, "", 5, 50);
        storeRangeProp.setLanguageKey(storageRangeLangKey);
        Property forceCompatProp = configFile.get(Configuration.CATEGORY_GENERAL, "forceCompatibility", forceCompatibilityDefault);
        forceCompatProp.setLanguageKey(forceCompatibilityLangKey);
        forceCompatProp.setRequiresWorldRestart(true);
        Property keepConfigProp = configFile.get(Configuration.CATEGORY_GENERAL, "keepConfigOnPickup", keepConfigOnPickupDefault);
        keepConfigProp.setLanguageKey(keepConfigOnPickupLangKey);
        Property compatModeProp = configFile.get(Configuration.CATEGORY_GENERAL, "compatibilityMode", compatibilityModeDefault, "", new String[]{Ref.COMPAT_MODE_VANILLA, Ref.COMPAT_MODE_DISCOVER, Ref.COMPAT_MODE_MANUAL});
        compatModeProp.setLanguageKey(compatibilityModeLangKey);
        Property compatListTypeProp = configFile.get(CATEGORY_MANUAL, "compatibilityListType", compatListTypeDefault, "", new String[]{Ref.MANUAL_COMPAT_TYPE_WHITE, Ref.MANUAL_COMPAT_TYPE_BLACK});
        compatListTypeProp.setLanguageKey(compatListTypeLangKey);
        Property compatListProp = configFile.get(CATEGORY_MANUAL, "compatibilityList", compatListDefault);
        compatListProp.setLanguageKey(compatListLangKey);
        compatListProp.setConfigEntryClass(CustomValidationArrayEntry.class);
        Property compatListIngameConfProp = configFile.get(CATEGORY_MANUAL, "compatibilityListIngameConf", compatListIngameConfDefault);
        compatListIngameConfProp.setLanguageKey(compatListIngameConfLangKey);

        storeRange = storeRangeProp.getInt();
        forceCompatibility = forceCompatProp.getBoolean();
        keepConfigOnPickup = keepConfigProp.getBoolean();
        compatibilityMode = compatModeProp.getString();
        compatListType = compatListTypeProp.getString();
        compatList = compatListProp.getStringList();
        compatListIngameConf = compatListIngameConfProp.getBoolean();
        compatListFqClassnamesCache = null;
        compatListClassnamesMatchesCache = null;

        configFile.getCategory(Configuration.CATEGORY_GENERAL).clear();
        configFile.getCategory(CATEGORY_MANUAL).clear();

        updateOldConfig();

        configFile.getCategory(Configuration.CATEGORY_GENERAL).put(storeRangeProp.getName(), storeRangeProp);
        configFile.getCategory(Configuration.CATEGORY_GENERAL).put(keepConfigProp.getName(), keepConfigProp);
        configFile.getCategory(Configuration.CATEGORY_GENERAL).put(compatModeProp.getName(), compatModeProp);
        configFile.getCategory(CATEGORY_MANUAL).put(compatListTypeProp.getName(), compatListTypeProp);
        configFile.getCategory(CATEGORY_MANUAL).put(compatListProp.getName(), compatListProp);
        configFile.getCategory(CATEGORY_MANUAL).put(compatListIngameConfProp.getName(), compatListIngameConfProp);

        rebuildClassnamesCache();
        if(configFile.hasChanged())
            configFile.save();
    }

    private static void updateOldConfig() {
        if (forceCompatibility) {
            forceCompatibility = false;
            compatibilityMode = Ref.COMPAT_MODE_DISCOVER;
        }
    }

    public static String[] getCompatList() {
        return ExDepotConfig.configFile.get(CATEGORY_MANUAL, "compatibilityList", compatListDefault).getStringList();
    }

    public static void setCompatList(String[] compatList) {
        Property prop = ExDepotConfig.configFile.get(CATEGORY_MANUAL, "compatibilityList", compatListDefault);
        prop.set(compatList);
        ExDepotConfig.compatList = prop.getStringList();
        rebuildClassnamesCache();
        if(configFile.hasChanged())
            configFile.save();
    }

    public static boolean compatListMatch(GuiScreen gui) {
        if (compatListFqClassnamesCache == null || compatListClassnamesMatchesCache == null) {
            ExDepotMod.LOGGER.error("CompatList was used without being initialized.");
            return false;
        }
        if (compatListFqClassnamesCache.contains(gui.getClass().getName())) {
            return true;
        }
        for (String glob : compatListClassnamesMatchesCache) {
            if (globMatch(glob, gui.getClass().getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean globMatch(String glob, String name) {
        String truncatedName = name;
        if (!glob.contains("*")) {
            return false;
        }
        for (String sub : glob.split("\\*")) {
            int idx = truncatedName.indexOf(sub);
            if (idx == -1) {
                return false;
            } else {
                truncatedName = truncatedName.substring(idx);
            }
        }
        // String.split does not split if the pattern is at the end of the string.
        if (glob.endsWith("*")) {
            truncatedName = "";
        }
        return truncatedName.isEmpty();
    }

    private static void rebuildClassnamesCache() {
        compatListFqClassnamesCache = new HashSet<>();
        compatListClassnamesMatchesCache = new ArrayList<>();
        for (String matchOrName : compatList) {
            if (matchOrName.contains("*")) {
                compatListClassnamesMatchesCache.add(matchOrName);
            } else {
                compatListFqClassnamesCache.add(matchOrName);
            }
        }
    }

    public static void addOrRemoveFromCompatList(GuiScreen gui) {
        if (compatListMatch(gui)) {
            removeCompatListMatchingRules(gui);
        } else {
            String[] newArr = new String[compatList.length + 1];
            System.arraycopy(compatList, 0, newArr, 0, compatList.length);
            newArr[compatList.length] = gui.getClass().getName();
            setCompatList(newArr);
        }
    }

    private static void removeCompatListMatchingRules(GuiScreen gui) {
        LinkedList<Integer> indicesToRemove = new LinkedList<>();
        for (int i=0; i<compatList.length; i++) {
            if (compatList[i].equals(gui.getClass().getName()) || globMatch(compatList[i], gui.getClass().getName())) {
                indicesToRemove.add(i);
            }
        }

        String[] newArr = new String[compatList.length - indicesToRemove.size()];
        for (int i=0, j=0; i<newArr.length; i++, j++) {
            while (indicesToRemove.size() > 0 && indicesToRemove.get(0) == i) {
                indicesToRemove.pop();
                i++;
            }
            if (i >= compatList.length) {
                break;
            }
            newArr[j] = compatList[i];
        }
        setCompatList(newArr);
    }
}
