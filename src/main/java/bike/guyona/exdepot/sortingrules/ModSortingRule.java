package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.helpers.GuiHelpers;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class ModSortingRule extends AbstractSortingRule {
    String modId;
    private ModContainer modCache;

    ModSortingRule(String modId) {
        this.modId = modId;
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
    public String getDisplayName() {
        return getMod().getName();
    }

    @Override
    public void draw(int left, int top, int zLevel) {
        Minecraft mc = Minecraft.getMinecraft();
        ModContainer mod = getMod();
        GuiHelpers.drawMod(left + StorageConfigGui.RULE_OFFSET,
                top, zLevel, mod, 20, 20);
        mc.fontRendererObj.drawString(
                mod.getName(),
                left + StorageConfigGui.ICON_WIDTH + StorageConfigGui.RULE_OFFSET,
                top + 5,
                0xFFFFFF);
    }

    @Override
    public String getTypeDisplayName() {
        return "Mod";
    }

    @Override
    public byte[] toBytes() {
        return modId.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof ModSortingRule) {
            ModSortingRule t = (ModSortingRule) o;
            return modId.compareTo(t.modId);
        } else {
            throw new ClassCastException(String.format("object being compared is not a %s", this.getClass().toString()));
        }
    }
}
