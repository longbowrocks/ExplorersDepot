package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.helpers.GuiHelpers;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ModSortingRule extends AbstractSortingRule {
    static final long serialVersionUID = 30;

    String modId;
    private ModContainer modCache;

    ModSortingRule(String modId) {
        this.modId = modId;
    }

    ModSortingRule(ModContainer mod) {
        this.modId = mod.getModId();
        this.modCache = mod;
    }

    ModContainer getMod() {
        if (modCache == null) {
            Loader loader = Loader.instance();
            for (ModContainer mod: loader.getModList()) {
                if (mod.getModId().equals(modId)) {
                    modCache = mod;
                    break;
                }
            }
        }
        return modCache;
    }

    @Override
    public int hashCode() {
        return modId.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ModSortingRule && modId.equals(((ModSortingRule) other).modId);
    }

    @Override
    public boolean matches(Object thing) {
        if (thing instanceof ModSortingRule) {
            return equals(thing);
        } else if (thing instanceof ModContainer) {
            return modId.equals(((ModContainer) thing).getModId());
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return getMod().getName();
    }

    @Override
    public void draw(int left, int top, float zLevel) {
        Minecraft mc = Minecraft.getMinecraft();
        ModContainer mod = getMod();
        GuiHelpers.drawMod(left,
                top, zLevel, mod, 20, 20);
        mc.fontRendererObj.drawString(
                getDisplayName(),
                left + StorageConfigGui.ICON_WIDTH,
                top + 5,
                0xFFFFFF);
    }

    @Override
    public String getTypeDisplayName() {
        return "Mod";
    }

    @Override
    public byte[] toBytes() {
        byte[] idBytes = modId.getBytes(StandardCharsets.UTF_8);
        ByteBuffer outBuf = ByteBuffer.allocate(Integer.SIZE / 8 + idBytes.length);
        outBuf.putInt(idBytes.length);
        outBuf.put(idBytes);
        return outBuf.array();
    }
}
