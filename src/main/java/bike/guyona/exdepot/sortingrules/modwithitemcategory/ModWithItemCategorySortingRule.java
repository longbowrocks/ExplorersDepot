package bike.guyona.exdepot.sortingrules.modwithitemcategory;

import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.helpers.AccessHelpers;
import bike.guyona.exdepot.helpers.GuiHelpers;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static bike.guyona.exdepot.helpers.ModSupportHelpers.DISALLOWED_CATEGORIES;

public class ModWithItemCategorySortingRule extends AbstractSortingRule {
    static final long serialVersionUID = 40;

    private String modId;
    private Integer category;
    private ModInfo modCache;
    private ItemGroup categoryCache;

    ModWithItemCategorySortingRule(String modId, Integer tabIndex) {
        this.modId = modId;
        this.category = tabIndex;
    }

    ModWithItemCategorySortingRule(ModInfo mod, ItemGroup tab) {
        this.modId = mod.getModId();
        this.modCache = mod;
        this.category = AccessHelpers.getTabIndex(tab);
        this.categoryCache = tab;
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

    ItemGroup getTab() {
        if (categoryCache == null) {
            for (ItemGroup tab:ItemGroup.GROUPS) {
                if (Arrays.asList(DISALLOWED_CATEGORIES).contains(tab)) {
                    continue;
                }
                if (category.equals(tab.getIndex())) {
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
        return getMod().getDisplayName()+" : "+ I18n.format(getTab().getTabLabel());
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
        return "ModCat";
    }

    @Override
    public byte[] toBytes() {
        byte[] modIdBytes = modId.getBytes(StandardCharsets.UTF_8);
        ByteBuffer outBuf = ByteBuffer.allocate(
                Integer.SIZE / 8 + modIdBytes.length +
                Integer.SIZE / 8);
        outBuf.putInt(modIdBytes.length);
        outBuf.put(modIdBytes);
        outBuf.putInt(category);
        return outBuf.array();
    }
}
