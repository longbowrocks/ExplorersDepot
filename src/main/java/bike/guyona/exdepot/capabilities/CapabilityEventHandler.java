package bike.guyona.exdepot.capabilities;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.Ref;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static bike.guyona.exdepot.ExDepotMod.DEPOT_CAPABILITY_RESOURCE;
import static bike.guyona.exdepot.helpers.ModSupportHelpers.blockEntityCompatible;

@Mod.EventBusSubscriber(modid = Ref.MODID)
public class CapabilityEventHandler {
    // TODO: Can go back to subscription events on the MOD event bus once forge fixes subscriptions.
//    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        ExDepotMod.LOGGER.info("Registering my capability");
        event.register(IDepotCapability.class);
    }

    @SubscribeEvent
    public static void attachTileCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        ExDepotMod.LOGGER.info("Attach capability event occurred for {}", event.getObject());
        if (blockEntityCompatible(event.getObject())) {
            ExDepotMod.LOGGER.info("Looks like this works");
            event.addCapability(DEPOT_CAPABILITY_RESOURCE, new DepotCapabilityProvider());
        }
    }
}
