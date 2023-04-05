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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

public class RulesList extends ObjectSelectionList<RulesList.Entry> {
    private static final int ICON_WIDTH = 16;
    Font font;
    ItemRenderer itemRenderer;
    Consumer<AbstractSortingRule> clickRuleAction;
    int itemHeaderIdx;

    public RulesList(Minecraft minecraft, int x, int y, int width, int height, int itemHeight, Consumer<AbstractSortingRule> clickRuleAction) {
        super(minecraft, width, height, y, y+height, itemHeight);
        this.font = minecraft.font;
        this.itemRenderer = minecraft.getItemRenderer();
        setLeftPos(x);
        setRenderBackground(false);
        setRenderTopAndBottom(false);
        this.clickRuleAction = clickRuleAction;
        this.itemHeaderIdx = 0;
    }

    public void updateHeightPinBottom(int newHeight) {
        this.updateSize(this.width, newHeight, this.y1-newHeight, this.y1);
    }

    public void updateHeightPinTop(int newHeight) {
        int leftPos = this.x0;
        this.updateSize(this.width, newHeight, this.y0, this.y0+newHeight);
        this.setLeftPos(leftPos);
    }

    public void init(IDepotCapability cap) {
        this.clearEntries();
        this.addHeaders();
        List<Entry> entries = this.children();
        for (ModSortingRule rule : cap.getRules(ModSortingRule.class)) {
            entries.add(this.itemHeaderIdx, new RulesList.ModEntry(rule));
            this.itemHeaderIdx += 1;
        }
        for (ItemSortingRule rule : cap.getRules(ItemSortingRule.class)) {
            entries.add(new RulesList.ItemEntry(rule));
        }
    }

    public void addHeaders() {
        this.clearEntries();
        this.addEntry(new RulesList.HeaderEntry("ui.translatable.mod"));
        this.addEntry(new RulesList.HeaderEntry("ui.translatable.item"));
        this.itemHeaderIdx = 1;
    }

    public void updateResults(List<IModInfo> modResults, List<Item> itemResults) {
        this.clearEntries();
        this.addHeaders();
        List<Entry> entries = this.children();
        for (IModInfo modResult : modResults) {
            entries.add(this.itemHeaderIdx, new RulesList.ModEntry(new ModSortingRule(modResult.getModId())));
            this.itemHeaderIdx += 1;
        }
        for (Item itemResult : itemResults) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(itemResult);
            if (itemId == null) {
                ExDepotMod.LOGGER.warn("IMPOSSIBLE: Item {} doesn't have an ID.", itemResult);
                continue;
            }
            this.addEntry(new RulesList.ItemEntry(new ItemSortingRule(itemId.toString())));
        }
    }

    public void insertEntry(AbstractSortingRule rule) {
        List<Entry> entries = this.children();
        if (rule instanceof ModSortingRule) {
            entries.add(this.itemHeaderIdx, new ModEntry((ModSortingRule)rule));
            this.itemHeaderIdx += 1;
        } else if (rule instanceof ItemSortingRule) {
            entries.add(new ItemEntry((ItemSortingRule)rule));
        }  else {
            ExDepotMod.LOGGER.warn("Can't insert a {} to {}.", rule.getClass(), this.getClass());
        }
    }

    public void removeEntry(AbstractSortingRule rule) {
        List<Entry> entries = this.children();
        for (int i=0; i<entries.size(); i++) {
            if (entries.get(i).value.equals(rule)) {
                if (entries.get(i).value instanceof ModSortingRule) {
                    this.itemHeaderIdx -= 1;
                }
                entries.remove(i);
                break;
            }
        }
    }

    public class Entry extends ObjectSelectionList.Entry<RulesList.Entry> {
        final Component name;
        final Object value;

        public Entry(String header) {
            this.name = Component.translatable(header);
            this.value = header;
        }

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
                    RulesList.this.clickRuleAction.accept((AbstractSortingRule)this.value);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public class HeaderEntry extends Entry {
        public HeaderEntry(String header) {
            super(header);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void render(PoseStack poseStack, int entryIdx, int entryTop, int entryLeft, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float partialTicks) {
            GuiComponent.drawString(poseStack, Minecraft.getInstance().font, this.name, entryLeft + 5, entryTop + 2, DepotRulesScreen.COLOR_WHITE_OPACITY_NONE);
        }
    }

    public class ItemEntry extends Entry {
        final ItemStack drawableItem;

        public ItemEntry(ItemSortingRule rule) {
            super(rule);
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(rule.getItemId()));
            if (item == null) {
                ExDepotMod.LOGGER.warn("IMPOSSIBLE: Item {} doesn't exist in the registry.", rule.getItemId());
                item = Items.AIR;
            }
            this.drawableItem = item.getDefaultInstance();
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
