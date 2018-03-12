package bike.guyona.exdepot.helpers;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.*;
import java.nio.ByteBuffer;

import static bike.guyona.exdepot.ExDepotMod.LOGGER;

public class NbtHelpers {
    public static byte[] toBytes(NBTTagCompound itemTags) {
        byte[] nbtArray;
        if (itemTags == null) {
            nbtArray = new byte[0];
        } else {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            DataOutput outStream = new DataOutputStream(buf);
            try {
                CompressedStreamTools.write(itemTags, outStream);
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error("Not sure what's up, but because of the above traceback I can't write NBT.");
            }
            nbtArray = buf.toByteArray();
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.SIZE / 8 + nbtArray.length);
        byteBuffer.putInt(nbtArray.length);
        byteBuffer.put(nbtArray);
        return byteBuffer.array();
    }

    public static NBTTagCompound fromBytes(ByteBuffer bbuf) {
        int nbtLength = bbuf.getInt();
        NBTTagCompound nbt;
        if (nbtLength == 0) {
            nbt = null;
        } else {
            byte[] nbtArray = new byte[nbtLength];
            bbuf.get(nbtArray);
            ByteArrayInputStream buf = new ByteArrayInputStream(nbtArray);
            DataInputStream inStream = new DataInputStream(buf);
            try {
                nbt = CompressedStreamTools.read(inStream);
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error("Not sure what's up, but because of the above traceback I can't read NBT.");
                nbt = null;
            }
        }
        return nbt;
    }
}
