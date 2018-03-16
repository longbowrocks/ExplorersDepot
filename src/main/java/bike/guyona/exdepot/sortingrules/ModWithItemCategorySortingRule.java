package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.helpers.GuiHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static bike.guyona.exdepot.helpers.ModSupportHelpers.DISALLOWED_CATEGORIES;

public class ModWithItemCategorySortingRule extends AbstractSortingRule {
    static final long serialVersionUID = 40;

    String modId;
    String category;
    private ModContainer modCache;
    private CreativeTabs categoryCache;

    ModWithItemCategorySortingRule(String modId, String tabLabel) {
        this.modId = modId;
        this.category = tabLabel;
    }

    ModWithItemCategorySortingRule(ModContainer mod, CreativeTabs tab) {
        this.modId = mod.getModId();
        this.modCache = mod;
        this.category = tab.getTabLabel();
        this.categoryCache = tab;
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

    CreativeTabs getTab() {
        if (categoryCache == null) {
            for (CreativeTabs tab:CreativeTabs.CREATIVE_TAB_ARRAY) {
                if (Arrays.asList(DISALLOWED_CATEGORIES).contains(tab)) {
                    continue;
                }
                if (tab.getTabLabel().equals(category)) {
                    categoryCache = tab;
                    break;
                }
            }
        }
        return categoryCache;
    }

    @Override
    public int hashCode() {
        return modId.hashCode() + category.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ModWithItemCategorySortingRule &&
                modId.equals(((ModWithItemCategorySortingRule) obj).modId) &&
                category.equals(((ModWithItemCategorySortingRule) obj).category));
    }

    @Override
    public boolean matches(Object thing) {
        if (thing instanceof ModWithItemCategorySortingRule) {
            return equals(thing);
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return getMod().getName()+" : "+ I18n.format(getTab().getTranslatedTabLabel());
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
        return "ModCat";
    }

    @Override
    public byte[] toBytes() {
        byte[] modIdBytes = modId.getBytes(StandardCharsets.UTF_8);
        byte[] categoryBytes = category.getBytes(StandardCharsets.UTF_8);
        ByteBuffer outBuf = ByteBuffer.allocate(
                Integer.SIZE / 8 + modIdBytes.length +
                Integer.SIZE / 8 + categoryBytes.length);
        outBuf.putInt(modIdBytes.length);
        outBuf.put(modIdBytes);
        outBuf.putInt(categoryBytes.length);
        outBuf.put(categoryBytes);
        return outBuf.array();
    }
}
