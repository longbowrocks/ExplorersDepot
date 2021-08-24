package bike.guyona.exdepot.sortingrules.mod;

import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.helpers.GuiHelpers;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ModSortingRule extends AbstractSortingRule {
    static final long serialVersionUID = 30;

    String modId;
    private ModInfo modCache;

    ModSortingRule(String modId) {
        this.modId = modId;
    }

    ModSortingRule(ModInfo mod) {
        this.modId = mod.getModId();
        this.modCache = mod;
    }

    ModInfo getMod() {
        if (modCache == null) {
            for (ModInfo mod: ModList.get().getMods()) {
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
        return getMod().getDisplayName();
    }

    @Override
    public void draw(int left, int top, float zLevel) {
        Minecraft mc = Minecraft.getInstance();
        ModInfo mod = getMod();
        GuiHelpers.drawMod(left,
                top, zLevel, mod, 20, 20);
        mc.fontRenderer.drawString(
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
