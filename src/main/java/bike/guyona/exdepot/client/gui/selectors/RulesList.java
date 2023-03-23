package bike.guyona.exdepot.client.gui.selectors;

import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.client.gui.DepotRulesScreen;
import bike.guyona.exdepot.client.helpers.GuiHelpers;
import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import bike.guyona.exdepot.sortingrules.item.ItemSortingRule;
import bike.guyona.exdepot.sortingrules.mod.ModSortingRule;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class RulesList extends ObjectSelectionList<RulesList.Entry> {
    private static final int ICON_WIDTH = 20;
    Font font;
    ItemRenderer itemRenderer;

    public RulesList(Minecraft minecraft, int x, int y, int width, int height, int itemHeight) {
        super(minecraft, width, height, y, y+height, itemHeight);
        this.font = minecraft.font;
        this.itemRenderer = minecraft.getItemRenderer();
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
                lastRuleIndexOfType = i+1;
            }
        }
        if (lastRuleIndexOfType == -1) {
            lastRuleIndexOfType = rules.size();
        }
        rules.add(lastRuleIndexOfType, new Entry(rule));
    }

    public class Entry extends ObjectSelectionList.Entry<RulesList.Entry> {
        final Component name;
        final ItemStack drawableItem;
        final ResourceLocation drawableMod;
        final Object value;

        public Entry(AbstractSortingRule rule) {
            this.name = rule.getDisplayName();
            this.drawableItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:chest")).getDefaultInstance();
            this.drawableMod = GuiHelpers.registerModLogoTexture("minecraft");
            this.value = rule;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.translatable("narrator.select", this.name);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void render(PoseStack poseStack, int entryIdx, int entryTop, int entryLeft, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
            if (this.value instanceof ItemSortingRule) {
                RulesList.this.itemRenderer.renderAndDecorateItem(this.drawableItem, entryLeft, entryTop);
                RulesList.this.itemRenderer.renderGuiItemDecorations(RulesList.this.font, this.drawableItem, entryLeft, entryTop, null);
            } else if (this.value instanceof  ModSortingRule) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, this.drawableMod);
                RenderSystem.enableDepthTest();
                blit(poseStack, entryLeft, entryTop, 0, 0, ICON_WIDTH, ICON_WIDTH, ICON_WIDTH, ICON_WIDTH);
            }
            GuiComponent.drawString(poseStack, Minecraft.getInstance().font, this.name, entryLeft + ICON_WIDTH, entryTop + 4, DepotRulesScreen.COLOR_WHITE_OPACITY_NONE);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                RulesList.this.removeEntry(this);
                return true;
            } else {
                return false;
            }
        }
    }
}
