package bike.guyona.exdepot.events;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.client.DepositItemsJuice;
import bike.guyona.exdepot.network.ViewDepotsMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static bike.guyona.exdepot.ExDepotMod.NETWORK_INSTANCE;


@Mod.EventBusSubscriber(modid = Ref.MODID, value = Dist.CLIENT)
public class EventHandler {
    public static final DepositItemsJuice JUICER = new DepositItemsJuice();
    private static long lastUpdatedViewableConfigs = 0;
    private static final int VIEWABLE_CONFIG_REFRESH_INTERVAL_MS = 5000;

    @SubscribeEvent
    static void onClientTick(TickEvent.ClientTickEvent event) {
        JUICER.handleClientTick();
        long curTime = System.currentTimeMillis();
        if (isIngame() && curTime > lastUpdatedViewableConfigs + VIEWABLE_CONFIG_REFRESH_INTERVAL_MS) {
            NETWORK_INSTANCE.sendToServer(new ViewDepotsMessage());
            lastUpdatedViewableConfigs = curTime;
        }
    }

    //Read DepotCap from NBT, back into BlockEntity.
    @SubscribeEvent
    static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {

    }

    private static boolean isIngame() {
        return Minecraft.getInstance().level != null;
    }
}
