package bike.guyona.exdepot.client.events;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.client.DepositItemsJuice;
import bike.guyona.exdepot.items.DepotConfiguratorWandBase;
import bike.guyona.exdepot.client.network.viewdepots.ViewDepotsCacheWhisperer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = Ref.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EventHandler {
    public static final DepositItemsJuice JUICER = new DepositItemsJuice();
    public static final ViewDepotsCacheWhisperer VIEW_DEPOTS_CACHE_WHISPERER = new ViewDepotsCacheWhisperer();

    @SubscribeEvent
    static void onClientTick(TickEvent.ClientTickEvent event) {
        JUICER.handleClientTick();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && DepotConfiguratorWandBase.isWand(player.getMainHandItem().getItem())) {
            if (isIngame() && VIEW_DEPOTS_CACHE_WHISPERER.isUpdateDue()) {
                VIEW_DEPOTS_CACHE_WHISPERER.triggerUpdateFromClient();
            }
        }else if (VIEW_DEPOTS_CACHE_WHISPERER.isActive()) {
            VIEW_DEPOTS_CACHE_WHISPERER.replaceParticles(new ArrayList<>());
        }
    }

    private static boolean isIngame() {
        return Minecraft.getInstance().level != null;
    }
}
