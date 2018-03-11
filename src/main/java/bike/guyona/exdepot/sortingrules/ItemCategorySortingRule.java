package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.helpers.GuiHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static bike.guyona.exdepot.helpers.ModSupportHelpers.DISALLOWED_CATEGORIES;

public class ItemCategorySortingRule extends AbstractSortingRule {
    String category;
    private CreativeTabs categoryCache;

    ItemCategorySortingRule(String tabLabel) {
        this.category = tabLabel;
    }

    ItemCategorySortingRule(CreativeTabs tab) {
        this.category = tab.getTabLabel();
        this.categoryCache = tab;
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
        return category.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ItemCategorySortingRule && category.equals(((ItemCategorySortingRule) other).category);
    }

    @Override
    public boolean matches(Object thing) {
        if (thing instanceof ItemCategorySortingRule) {
            return equals(thing);
        } else if (thing instanceof CreativeTabs) {
            return category.equals(((CreativeTabs) thing).getTabLabel());
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return I18n.format(getTab().getTranslatedTabLabel());
    }

    @Override
    public void draw(int left, int top, float zLevel) {
        Minecraft mc = Minecraft.getMinecraft();
        CreativeTabs tab = getTab();
        GuiHelpers.drawItem(left,
                top, tab.getIconItemStack(), mc.fontRendererObj);
        mc.fontRendererObj.drawString(
                getDisplayName(),
                left + StorageConfigGui.ICON_WIDTH,
                top + 5,
                0xFFFFFF);
    }

    @Override
    public String getTypeDisplayName() {
        return "Category";
    }

    @Override
    public byte[] toBytes() {
        byte[] idBytes = category.getBytes(StandardCharsets.UTF_8);
        ByteBuffer outBuf = ByteBuffer.allocate(Integer.SIZE / 8 + idBytes.length);
        outBuf.putInt(idBytes.length);
        outBuf.put(idBytes);
        return outBuf.array();
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof ItemCategorySortingRule) {
            ItemCategorySortingRule t = (ItemCategorySortingRule) o;
            return category.compareTo(t.category);
        } else {
            throw new ClassCastException(String.format("object being compared is not a %s", this.getClass().toString()));
        }
    }
}
