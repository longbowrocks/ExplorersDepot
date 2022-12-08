package bike.guyona.exdepot.client;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.client.particles.DepositingItemParticleProvider;
import bike.guyona.exdepot.client.particles.ViewDepotParticleProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ref.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModInit {
    // https://docs.minecraftforge.net/en/1.19.x/gameeffects/particles/#particleprovider
    @SubscribeEvent
    static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.register(ExDepotMod.DEPOSITING_ITEM_PARTICLE_TYPE.get(), new DepositingItemParticleProvider());
        event.register(ExDepotMod.VIEW_DEPOT_PARTICLE_TYPE.get(), new ViewDepotParticleProvider());
    }
}
