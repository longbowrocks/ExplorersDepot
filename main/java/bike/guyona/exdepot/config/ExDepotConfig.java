package bike.guyona.exdepot.config;


import bike.guyona.exdepot.Ref;
import net.minecraftforge.common.config.Config;

@Config(modid= Ref.MODID)
public class ExDepotConfig {
    @Config.Comment("Max distance to select chests for storage at")
    @Config.RangeInt(min = 5,max = 50)
    @Config.Name("Storage Range")
    public static int storeRange=10;
}
