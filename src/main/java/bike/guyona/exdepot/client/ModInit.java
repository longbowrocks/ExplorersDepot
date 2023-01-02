package bike.guyona.exdepot.client;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.client.keys.KeybindHandler;
import bike.guyona.exdepot.client.particles.DepositingItemParticleProvider;
import bike.guyona.exdepot.client.particles.ViewDepotParticleProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ref.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModInit {
    // https://docs.minecraftforge.net/en/1.19.x/gameeffects/particles/#particleprovider
    @SubscribeEvent
    static void registerParticleProviders(ParticleFactoryRegisterEvent event) {
        ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;
        particleEngine.register(ExDepotMod.DEPOSITING_ITEM_PARTICLE_TYPE.get(), new DepositingItemParticleProvider());
        particleEngine.register(ExDepotMod.VIEW_DEPOT_PARTICLE_TYPE.get(), new ViewDepotParticleProvider());
        // TODO: probably can't do this
        KeybindHandler.onRegisterKeyMappings();
    }
}
