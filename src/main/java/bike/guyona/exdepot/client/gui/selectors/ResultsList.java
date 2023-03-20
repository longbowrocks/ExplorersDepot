package bike.guyona.exdepot.client.gui.selectors;

import bike.guyona.exdepot.client.gui.DepotRulesScreen;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.item.ItemSortingRule;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ResultsList extends ObjectSelectionList<ResultsList.Entry> {
    Consumer<AbstractSortingRule> pushRule;

    public ResultsList(Minecraft minecraft, int x, int y, int width, int height, int itemHeight, Consumer<AbstractSortingRule> pushRule) {
        super(minecraft, width, height, y, y+height, itemHeight);
        setLeftPos(x);
        setRenderBackground(false);
        setRenderTopAndBottom(false);
        this.pushRule = pushRule;
    }

    public void updateResults(List<IModInfo> modResults, List<Item> itemResults) {
        this.clearEntries();
        if (modResults.size() > 0) {
            this.addEntry(new ResultsList.Entry("ui.translatable.mod"));
        }
        for (IModInfo modResult : modResults) {
            this.addEntry(new ResultsList.Entry(new ModSortingRule(modResult.getModId())));
        }
        if (itemResults.size() > 0) {
            this.addEntry(new ResultsList.Entry("ui.translatable.item"));
        }
        for (Item itemResult : itemResults) {
            this.addEntry(new ResultsList.Entry(new ItemSortingRule(ForgeRegistries.ITEMS.getKey(itemResult).toString())));
        }
    }

    public void updateHeightPinTop(int newHeight) {
        int leftPos = this.x0;
        this.updateSize(this.width, newHeight, this.y0, this.y0+newHeight);
        this.setLeftPos(leftPos);
    }

    public class Entry extends ObjectSelectionList.Entry<ResultsList.Entry> {
        public enum ResultType {
            HEADER,
            MOD,
            ITEM
        }
        final Component name;
        final ResultType type;
        final Object value;

        public Entry(String header) {
            this.name = Component.translatable(header);
            this.type = ResultType.HEADER;
            this.value = header;
        }
        public Entry(AbstractSortingRule rule) {
            this.name = rule.getDisplayName();
            this.type = rule instanceof ModSortingRule ? ResultType.MOD : ResultType.ITEM;
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
                ResultsList.this.setSelected(this);
                if (type != ResultType.HEADER) {
                    ResultsList.this.pushRule.accept((AbstractSortingRule)this.value);
                }
                return true;
            } else {
                return false;
            }
        }
    }
}
