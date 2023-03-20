package bike.guyona.exdepot.client.gui.selectors;

import bike.guyona.exdepot.Ref;
import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.client.gui.DepotRulesScreen;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.item.ItemSortingRule;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class RulesList extends ObjectSelectionList<RulesList.Entry> {

    public RulesList(Minecraft minecraft, int x, int y, int width, int height, int itemHeight) {
        super(minecraft, width, height, y, y+height, itemHeight);
        setLeftPos(x);
        setRenderBackground(false);
        setRenderTopAndBottom(false);
    }

    public void updateHeightPinBottom(int newHeight) {
        this.updateSize(this.width, newHeight, this.y1-newHeight, this.y1);
    }

    /**
     * Show rulesbox working without any real data
     */
    public void init(IDepotCapability cap) {
        for (ModSortingRule rule : cap.getRules(ModSortingRule.class)) {
            this.addEntry(new RulesList.Entry(rule));
        }
        for (ItemSortingRule rule : cap.getRules(ItemSortingRule.class)) {
            this.addEntry(new RulesList.Entry(rule));
        }
    }

    public void insertEntry(AbstractSortingRule rule) {
        int lastRuleIndexOfType = -1;
        List<Entry> rules = this.children();
        for (int i=0; i<rules.size(); i++) {
            if (rules.get(i).value.getClass() == rule.getClass()) {
                lastRuleIndexOfType = i;
            }
        }
        if (lastRuleIndexOfType == -1) {
            lastRuleIndexOfType = rules.size();
        }
        rules.add(lastRuleIndexOfType, new Entry(rule));
    }

    public class Entry extends ObjectSelectionList.Entry<RulesList.Entry> {
        final Component name;
        final Object value;

        public Entry(AbstractSortingRule rule) {
            this.name = rule.getDisplayName();
            this.value = rule;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.translatable("narrator.select", this.name);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void render(PoseStack poseStack, int entryIdx, int entryTop, int entryLeft, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
            GuiComponent.drawString(poseStack, Minecraft.getInstance().font, this.name, entryLeft + 5, entryTop + 2, DepotRulesScreen.COLOR_WHITE_OPACITY_NONE);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                RulesList.this.setSelected(this);
                return true;
            } else {
                return false;
            }
        }
    }
}
