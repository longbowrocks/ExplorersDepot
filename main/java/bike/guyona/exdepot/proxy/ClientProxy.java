package bike.guyona.exdepot.proxy;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.keys.KeyBindings;
import bike.guyona.exdepot.network.StoreItemsMessage;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


import static bike.guyona.exdepot.ExDepotMod.INSIDE_JOKES;
import static bike.guyona.exdepot.ExDepotMod.instance;

/**
 * Created by longb on 7/10/2017.
 */
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        KeyBindings.init();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        super.serverStarting(event);
    }

    @Override
    public void serverStopping(FMLServerStoppingEvent event) {
        super.serverStopping(event);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(KeyBindings.dumpItems.isPressed()){
            String chat = INSIDE_JOKES[(int)(Math.random()*INSIDE_JOKES.length)];
            Minecraft.getMinecraft().player.sendChatMessage(chat);
            ExDepotMod.NETWORK.sendToServer(new StoreItemsMessage());
        }
    }

    @SubscribeEvent
    public void onTick(@NotNull TickEvent.ClientTickEvent tick) {
        if(tick.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            if(mc.world != null && mc.player != null) {
                if(mc.currentScreen != null) {
                    instance.onTickInGUI(mc.currentScreen);
                } else {
                    instance.buttonAdded = false;
                }
            }
        }
    }
}
