package bike.guyona.exdepot.sortingrules;

import bike.guyona.exdepot.gui.StorageConfigGui;
import bike.guyona.exdepot.helpers.AccessHelpers;
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
    static final long serialVersionUID = 10;

    Integer category;
    private CreativeTabs categoryCache;

    ItemCategorySortingRule(int tabIndex) {
        this.category = tabIndex;
    }

    ItemCategorySortingRule(CreativeTabs tab) {
        this.category = AccessHelpers.getTabIndex(tab);
        this.categoryCache = tab;
    }

    CreativeTabs getTab() {
        if (categoryCache == null) {
            for (CreativeTabs tab:CreativeTabs.CREATIVE_TAB_ARRAY) {
                if (Arrays.asList(DISALLOWED_CATEGORIES).contains(tab)) {
                    continue;
                }
                if (category.equals(tab.getTabIndex())) {
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
            return category.equals(AccessHelpers.getTabIndex((CreativeTabs) thing));
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
                top, tab.getIconItemStack(), mc.fontRenderer);
        mc.fontRenderer.drawString(
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
        ByteBuffer outBuf = ByteBuffer.allocate(Integer.SIZE / 8);
        outBuf.putInt(category);
        return outBuf.array();
    }
}
