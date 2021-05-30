package bike.guyona.exdepot.config;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static bike.guyona.exdepot.Ref.CATEGORY_MANUAL;

@Mod.EventBusSubscriber(modid = Ref.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ExDepotConfig {
    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;
    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }
    public static int storeRange;
    public static boolean forceCompatibility;
    public static boolean keepConfigOnPickup;
    public static String compatibilityMode;
    public static String compatListType;
    public static String[] compatList;
    public static boolean compatListIngameConf;

    private static Set<String> compatListFqClassnamesCache;
    private static List<String> compatListClassnamesMatchesCache;

    public static void bakeConfig() {
        storeRange = CLIENT.storeRange.get();
        forceCompatibility = CLIENT.forceCompatibility.get();
        keepConfigOnPickup = CLIENT.keepConfigOnPickup.get();
        compatibilityMode = CLIENT.compatibilityMode.get();
        compatListType = CLIENT.compatListType.get();
        compatList = CLIENT.compatList.get();
        compatListIngameConf = CLIENT.compatListIngameConf.get();

        rebuildClassnamesCache();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
        if (configEvent.getConfig().getSpec() == CLIENT_SPEC) {
            bakeConfig();
        }
    }

    public static boolean compatListMatch(TileEntity tileEntity) {
        if (compatListFqClassnamesCache == null || compatListClassnamesMatchesCache == null) {
            ExDepotMod.LOGGER.error("CompatList was used without being initialized.");
            return false;
        }
        if (compatListFqClassnamesCache.contains(tileEntity.getClass().getName())) {
            return true;
        }
        for (String glob : compatListClassnamesMatchesCache) {
            if (globMatch(glob, tileEntity.getClass().getName())) {
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

    public static void setCompatList(String[] compatList) {
        ExDepotMod.LOGGER.error("\n\n\n\nTHIS CANNOT WORK\n\n\n\n");
        //TODO: fix this. I need to test to see how to modify config ingame.
    }

    public static void addOrRemoveFromCompatList(TileEntity tileEntity) {
        if (compatListMatch(tileEntity)) {
            removeCompatListMatchingRules(tileEntity);
        } else {
            String[] newArr = new String[compatList.length + 1];
            System.arraycopy(compatList, 0, newArr, 0, compatList.length);
            newArr[compatList.length] = tileEntity.getClass().getName();
            setCompatList(newArr);
        }
    }

    private static void removeCompatListMatchingRules(TileEntity tileEntity) {
        LinkedList<Integer> indicesToRemove = new LinkedList<>();
        for (int i=0; i<compatList.length; i++) {
            if (compatList[i].equals(tileEntity.getClass().getName()) || globMatch(compatList[i], tileEntity.getClass().getName())) {
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
