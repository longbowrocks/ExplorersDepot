package bike.guyona.exdepot.network;

import bike.guyona.exdepot.capability.StorageConfig;
import bike.guyona.exdepot.gui.ezview.EasyViewConfigTablet;
import bike.guyona.exdepot.proxy.ClientProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;
import static bike.guyona.exdepot.ExDepotMod.proxy;
import static bike.guyona.exdepot.proxy.ClientProxy.openConfigurationGui;

public class EasyViewConfigsRequestResponse implements IMessage, IMessageHandler<EasyViewConfigsRequestResponse, IMessage> {
    private StorageConfig storageConfig;
    private BlockPos storageConfigLocation;

    public EasyViewConfigsRequestResponse() {
        this.storageConfig = null;
        this.storageConfigLocation = null;
    }

    public EasyViewConfigsRequestResponse(StorageConfig storageConfig, BlockPos storageConfigLocation) {
        this.storageConfig = storageConfig;
        this.storageConfigLocation = storageConfigLocation;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        LOGGER.info("Sending over {}", storageConfigLocation);
        if (storageConfigLocation == null) {
            buf.writeInt(0);
            return;
        }
        buf.writeInt(1);

        buf.writeInt(storageConfigLocation.getX());
        buf.writeInt(storageConfigLocation.getY());
        buf.writeInt(storageConfigLocation.getZ());

        byte[] bytes = storageConfig.toBytes();
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int count = buf.readInt();
        if (count == 0) {
            return;
        }

        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        storageConfigLocation = new BlockPos(x, y, z);

        int objLength = buf.readInt();
        byte[] bytes = new byte[objLength];
        buf.readBytes(bytes);
        storageConfig = StorageConfig.fromBytes(bytes);
        LOGGER.info("Received {}", storageConfigLocation);
    }

    @Override
    public IMessage onMessage(EasyViewConfigsRequestResponse message, MessageContext ctx) {
        avoidClassNotFound(message, ctx);
        return null;
    }

    @SideOnly(Side.CLIENT)
    private IMessage avoidClassNotFound(EasyViewConfigsRequestResponse message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Minecraft mc = Minecraft.getMinecraft();
            if(mc.world != null && mc.player != null) {
                ((ClientProxy)proxy).setViewableConfig(message.storageConfig, message.storageConfigLocation);
            }
        });
        // No response packet
        return null;
    }
}
