package bike.guyona.exdepot.client.gui.selectors;

import bike.guyona.exdepot.ExDepotMod;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.antlr.v4.tool.Rule;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

public class RulesList extends ObjectSelectionList<RulesList.Entry> {
    private static final int ICON_WIDTH = 16;
    Font font;
    ItemRenderer itemRenderer;
    Consumer<AbstractSortingRule> popRule;

    public RulesList(Minecraft minecraft, int x, int y, int width, int height, int itemHeight, Consumer<AbstractSortingRule> popRule) {
        super(minecraft, width, height, y, y+height, itemHeight);
        this.font = minecraft.font;
        this.itemRenderer = minecraft.getItemRenderer();
        setLeftPos(x);
        setRenderBackground(false);
        setRenderTopAndBottom(false);
        this.popRule = popRule;
    }

    public void updateHeightPinBottom(int newHeight) {
        this.updateSize(this.width, newHeight, this.y1-newHeight, this.y1);
    }

    /**
     * Show rulesbox working without any real data
     */
    public void init(IDepotCapability cap) {
        for (ModSortingRule rule : cap.getRules(ModSortingRule.class)) {
            this.addEntry(new RulesList.ModEntry(rule));
        }
        for (ItemSortingRule rule : cap.getRules(ItemSortingRule.class)) {
            this.addEntry(new RulesList.ItemEntry(rule));
        }
    }

    public void insertEntry(AbstractSortingRule rule) {
        int lastRuleIndexOfType = -1;
        List<Entry> entries = this.children();
        for (int i=0; i<entries.size(); i++) {
            if (entries.get(i).value.getClass() == rule.getClass()) {
                lastRuleIndexOfType = i+1;
            }
        }
        if (lastRuleIndexOfType == -1) {
            lastRuleIndexOfType = entries.size();
        }
        if (rule instanceof ItemSortingRule) {
            entries.add(lastRuleIndexOfType, new ItemEntry((ItemSortingRule)rule));
        } else if (rule instanceof ModSortingRule) {
            entries.add(lastRuleIndexOfType, new ModEntry((ModSortingRule)rule));
        } else {
            ExDepotMod.LOGGER.warn("Can't insert a {} to {}.", rule.getClass(), this.getClass());
        }
    }

    public void removeEntry(AbstractSortingRule rule) {
        List<Entry> entries = this.children();
        for (int i=0; i<entries.size(); i++) {
            if (entries.get(i).value.equals(rule)) {
                entries.remove(i);
                break;
            }
        }
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
            GuiComponent.drawString(poseStack, Minecraft.getInstance().font, this.name, entryLeft + ICON_WIDTH * 2, entryTop + 4, DepotRulesScreen.COLOR_WHITE_OPACITY_NONE);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                if (this.value instanceof AbstractSortingRule) {
                    RulesList.this.popRule.accept((AbstractSortingRule)this.value);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public class ItemEntry extends Entry {
        final ItemStack drawableItem;

        public ItemEntry(ItemSortingRule rule) {
            super(rule);
            this.drawableItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(rule.getItemId())).getDefaultInstance();
        }

        @Override
        @ParametersAreNonnullByDefault
        public void render(PoseStack poseStack, int entryIdx, int entryTop, int entryLeft, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
            RulesList.this.itemRenderer.renderAndDecorateItem(this.drawableItem, entryLeft, entryTop);
            RulesList.this.itemRenderer.renderGuiItemDecorations(RulesList.this.font, this.drawableItem, entryLeft, entryTop, null);
            super.render(poseStack, entryIdx, entryTop, entryLeft, entryWidth, entryHeight, mouseX, mouseY, isHovered, partialTicks);
        }
    }

    public class ModEntry extends Entry {
        final ResourceLocation drawableMod;

        public ModEntry(ModSortingRule rule) {
            super(rule);
            this.drawableMod = GuiHelpers.registerModLogoTexture(rule.getModId());
        }

        @Override
        @ParametersAreNonnullByDefault
        public void render(PoseStack poseStack, int entryIdx, int entryTop, int entryLeft, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, this.drawableMod);
            RenderSystem.enableDepthTest();
            blit(poseStack, entryLeft, entryTop, 0, 0, ICON_WIDTH, ICON_WIDTH, ICON_WIDTH, ICON_WIDTH);
            super.render(poseStack, entryIdx, entryTop, entryLeft, entryWidth, entryHeight, mouseX, mouseY, isHovered, partialTicks);
        }
    }
}
