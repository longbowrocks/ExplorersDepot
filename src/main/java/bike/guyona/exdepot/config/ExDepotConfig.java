package bike.guyona.exdepot.config;


import bike.guyona.exdepot.Ref;
import net.minecraftforge.common.config.Config;

@Config(modid= Ref.MODID)
public class ExDepotConfig {
    @Config.Comment("Max distance to select chests for storage at")
    @Config.RangeInt(min = 5,max = 50)
    @Config.LangKey("exdepot.config.storageRange")
    public static int storeRange=10;

    @Config.RequiresWorldRestart
    @Config.Comment("Force all containers to work, but may have unexpected results")
    @Config.LangKey("exdepot.config.forceCompatibility")
    public static boolean forceCompatibility=false;

    @Config.Comment("Keep config when picking up a chest")
    @Config.LangKey("exdepot.config.pickupKeepsConfig")
    public static boolean keepConfigOnPickup=false;
}
