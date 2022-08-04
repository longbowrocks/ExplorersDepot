package bike.guyona.exdepot.config;

import bike.guyona.exdepot.helpers.CompatabilityMode;
import net.minecraftforge.common.ForgeConfigSpec;

// copy-ravioli https://github.com/Momo-Studios/Cold-Sweat/blob/029bb5abca7ef6e77fc8049932bf251a17f2dc3c/src/main/java/dev/momostudios/coldsweat/config/WorldSettingsConfig.java
public class ExDepotConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<Integer> storeRange;
    public static final ForgeConfigSpec.ConfigValue<Boolean> keepConfigOnPickup;
    public static final ForgeConfigSpec.ConfigValue<CompatabilityMode> compatibilityMode;

    static {
        BUILDER.push("General");
        storeRange = BUILDER.comment("When you press the deposit button, you items will be sorted into depots within this distance")
                .translation("exdepot.config.storageRange")
                .defineInRange("storageRange", 10, 5, 50);
        keepConfigOnPickup = BUILDER.comment("If true, depots remember their settings when picked up.")
                .translation("exdepot.config.keepConfigOnPickup")
                .define("keepConfigOnPickup", false);
        compatibilityMode = BUILDER.comment("Vanilla: mod will only work with vanilla containers.\nDiscover: mod will do its best to make all reasonable containers work.\nManual: You decide what works with a GUI whitelist or blacklist.")
                .translation("exdepot.config.compatibilityMode")
                .worldRestart()
                .defineEnum("compatibilityMode", CompatabilityMode.VANILLA);
        SPEC = BUILDER.build();
    }
}
