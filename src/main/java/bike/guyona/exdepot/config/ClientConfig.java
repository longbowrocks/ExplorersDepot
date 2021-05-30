package bike.guyona.exdepot.config;

import bike.guyona.exdepot.Ref;
import net.minecraftforge.common.ForgeConfigSpec;

import static bike.guyona.exdepot.Ref.CATEGORY_MANUAL;

public class ClientConfig {
    private static final int storeRangeDefault=10;
    public ForgeConfigSpec.IntValue storeRange;
    private static final String storageRangeLangKey = "exdepot.config.storageRange";

    private static final boolean forceCompatibilityDefault = false;
    @Deprecated
    public ForgeConfigSpec.BooleanValue forceCompatibility;
    private static final String forceCompatibilityLangKey = "exdepot.config.forceCompatibility";

    private static final boolean keepConfigOnPickupDefault = false;
    public ForgeConfigSpec.BooleanValue keepConfigOnPickup;
    private static final String keepConfigOnPickupLangKey = "exdepot.config.keepConfigOnPickup";

    private static final String compatibilityModeDefault = Ref.COMPAT_MODE_VANILLA;
    public ForgeConfigSpec.ConfigValue<String> compatibilityMode;
    private static final String compatibilityModeLangKey = "exdepot.config.compatibilityMode";

    private static final String compatListTypeDefault = Ref.MANUAL_COMPAT_TYPE_BLACK;
    public ForgeConfigSpec.ConfigValue<String> compatListType;
    private static final String compatListTypeLangKey = "exdepot.config.compatListType";

    private static final String[] compatListDefault = new String[]{};
    public ForgeConfigSpec.ConfigValue<String[]> compatList;
    private static final String compatListLangKey = "exdepot.config.compatList";

    private static final boolean compatListIngameConfDefault = false;
    public ForgeConfigSpec.BooleanValue compatListIngameConf;
    private static final String compatListIngameConfLangKey = "exdepot.config.compatListIngameConf";

    public ClientConfig(ForgeConfigSpec.Builder builder) {
        storeRange = builder
                .comment("aBoolean usage description")
                .translation(storageRangeLangKey)
                .defineInRange("storageRange", storeRangeDefault, 5, 50);
        forceCompatibility = builder
                .comment("aBoolean usage description")
                .translation(forceCompatibilityLangKey)
                .define("forceCompatibility", forceCompatibilityDefault);
        keepConfigOnPickup = builder
                .comment("aBoolean usage description")
                .translation(keepConfigOnPickupLangKey)
                .define("keepConfigOnPickup", keepConfigOnPickupDefault);
        compatibilityMode = builder
                .comment("aBoolean usage description")
                .translation(compatibilityModeLangKey)
                .define("compatibilityMode", compatibilityModeDefault);

        builder.push(CATEGORY_MANUAL);  // "manualSettings", "exdepot.config.compatListSettings"
        compatListType = builder
                .comment("aBoolean usage description")
                .translation(compatListTypeLangKey)
                .define("compatibilityListType", compatListTypeDefault);
        // https://forums.minecraftforge.net/topic/80318-115-config-behaviour/?do=findComment&comment=382694
        compatList = builder
                .comment("aBoolean usage description")
                .translation(compatListLangKey)
                .define("compatibilityList", compatListDefault);
        compatListIngameConf = builder
                .comment("aBoolean usage description")
                .translation(compatListIngameConfLangKey)
                .define("compatibilityListIngameConf", compatListIngameConfDefault);
        builder.pop();
    }
}
