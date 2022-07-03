package bike.guyona.exdepot.events;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.client.DepositItemsJuice;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Ref.MODID, value = Dist.CLIENT)
public class EventHandler {
    public static final DepositItemsJuice JUICER = new DepositItemsJuice();

    @SubscribeEvent
    static void onClientTick(TickEvent.ClientTickEvent event) {
        JUICER.handleClientTick();
    }
}
