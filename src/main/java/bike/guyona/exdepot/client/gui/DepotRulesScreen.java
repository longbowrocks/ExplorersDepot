package bike.guyona.exdepot.client.gui;

import bike.guyona.exdepot.capabilities.IDepotCapability;
import bike.guyona.exdepot.client.gui.buttons.ExDepotImageButton;
import bike.guyona.exdepot.client.gui.selectors.ResultsList;
import bike.guyona.exdepot.client.gui.selectors.RulesList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class DepotRulesScreen extends Screen {
    public static final int COLOR_WHITE_OPACITY_NONE = constructAlphaRGB((byte)0, (byte)255, (byte)255, (byte)255);
    public static final int COLOR_BLACK_OPACITY_MEDIUM = constructAlphaRGB((byte)192, (byte)16,(byte)16,(byte)16);
    public static final int COLOR_BLACK_OPACITY_HEAVY = constructAlphaRGB((byte)208, (byte)16,(byte)16,(byte)16);
    public static final int COLOR_DARK_GREEN_OPACITY_HEAVY = constructAlphaRGB((byte)208, (byte)16,(byte)32,(byte)16);

    private static final int MIN_ELEMENT_SEPARATION = 10;

    private boolean hasUnsavedChanges;
    private IDepotCapability savedDepotRules;
    private IDepotCapability depotRules;

    private EditBox searchField;
    private ResultsList resultsBox;
    private ImageButton ezConfigButton;
    private ImageButton saveConfigButton;
    private ImageButton clearConfigButton;
    private RulesList rulesBox;

    // keyPressed is called for all keys on an EditBox, but EditBox only reacts to Delete in that function.
    // ASCII chars are added by charTyped.
    private boolean searchFieldChanged = false;
    private int targetSearchResultsHeight = 0;
    private int searchResultsHeight = targetSearchResultsHeight;
    @NotNull
    private List<IModInfo> modResults = new ArrayList<>();
    @NotNull
    private List<Item> itemResults = new ArrayList<>();

    public DepotRulesScreen(Component parentScreen) {
        super(Component.translatable("exdepot.gui.depotrules.title"));
        this.hasUnsavedChanges = false;
    }

    /**
     * Initializes GUI state (ie widgets) from data state (ie DepotCapability).
     */
    @Override
    protected void init() {
        Minecraft mc = Minecraft.getInstance();
        int xOffset = MIN_ELEMENT_SEPARATION;
        int yOffset = MIN_ELEMENT_SEPARATION;
        // Create the searchy bits that add rules to the main list
        searchField = new EditBox(mc.font,
                xOffset, yOffset, 200, ExDepotImageButton.BUTTON_HEIGHT, Component.literal("Hi there!"));
        this.setFocused(searchField);
        searchField.setFocus(true);
        resultsBox = new ResultsList(mc, xOffset, yOffset + ExDepotImageButton.BUTTON_HEIGHT, 200,searchResultsHeight, ExDepotImageButton.BUTTON_HEIGHT);
        xOffset += MIN_ELEMENT_SEPARATION + searchField.getWidth();
        // Create my buttons
        saveConfigButton = new ExDepotImageButton(
                xOffset, yOffset, ExDepotImageButton.FLOPPY_DISK_BIDX,
                (button) -> {},
                Component.translatable("exdepot.gui.depotrules.tooltip.save"),
                this
        );
        xOffset += MIN_ELEMENT_SEPARATION + saveConfigButton.getWidth();
        ezConfigButton = new ExDepotImageButton(
                xOffset, yOffset, ExDepotImageButton.CHEST_AND_GEAR_BIDX,
                (button) -> {},
                Component.translatable("exdepot.gui.depotrules.tooltip.frominventory"),
                this
        );
        xOffset += MIN_ELEMENT_SEPARATION + ezConfigButton.getWidth();
        clearConfigButton = new ExDepotImageButton(
                xOffset, yOffset, ExDepotImageButton.FLOPPY_DISK_BIDX,
                (button) -> {},
                Component.translatable("exdepot.gui.depotrules.tooltip.clear"),
                this
        );
        xOffset = MIN_ELEMENT_SEPARATION;
        yOffset = MIN_ELEMENT_SEPARATION + ExDepotImageButton.BUTTON_HEIGHT + MIN_ELEMENT_SEPARATION;
        // Create the box in which current rule selections are displayed
        rulesBox = new RulesList(
                minecraft,
                xOffset, yOffset, width - 2 * MIN_ELEMENT_SEPARATION,
                getRealEstateHeight(),
                ExDepotImageButton.BUTTON_HEIGHT
        );
        rulesBox.dummyInit();

        this.addRenderableWidget(searchField);
        this.addRenderableWidget(resultsBox);
        this.addRenderableWidget(saveConfigButton);
        this.addRenderableWidget(ezConfigButton);
        this.addRenderableWidget(clearConfigButton);
        this.addRenderableWidget(rulesBox);
    }

    @Override
    public void tick() {
        this.hasUnsavedChanges = this.savedDepotRules != null && !this.savedDepotRules.equals(this.depotRules);
        this.searchField.tick();
        if (this.searchFieldChanged) {
            this.updateResults();
            this.updateResultsHeight();
            this.searchFieldChanged = false;
        }
        if (this.targetSearchResultsHeight - this.searchResultsHeight > 20) {
            this.searchResultsHeight += 10;
        } else {
            this.searchResultsHeight += (this.targetSearchResultsHeight - this.searchResultsHeight) / 2;
        }
        this.resultsBox.updateHeightPinTop(this.searchResultsHeight);
        this.rulesBox.updateHeightPinBottom(getRealEstateHeight() - this.searchResultsHeight);
    }

    @Override
    public boolean keyPressed(int key, int mouseX, int mouseY) {
        if (this.getFocused() == this.searchField) {
            this.searchField.keyPressed(key, mouseX, mouseY);
            this.searchFieldChanged = true;
            return true;
        }
        return super.keyPressed(key, mouseX, mouseY);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicksProbably) {
        this.renderBackground(poseStack);
        for(Widget widget : this.renderables) {
            widget.render(poseStack, mouseX, mouseY, partialTicksProbably);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public void renderBackground(PoseStack poseStack) {
        int upper_background_color = COLOR_BLACK_OPACITY_MEDIUM;
        int lower_background_color = COLOR_BLACK_OPACITY_HEAVY;
        if (this.hasUnsavedChanges) {
            lower_background_color = COLOR_DARK_GREEN_OPACITY_HEAVY;
        }
        this.fillGradient(poseStack, 0, 0, this.width, this.height, upper_background_color, lower_background_color);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ScreenEvent.BackgroundRendered(this, poseStack));
    }

    private static int constructAlphaRGB(byte alpha, byte r, byte g, byte b) {
        return alpha<<24 | r<<16 | g<<8 | b;
    }

    private void updateResults() {
        // if not tokenTreeCache; updateTokenTreeCache()
        modResults = new ArrayList<>();
        String currentFilter = searchField.getValue();
        for (IModInfo modInfo : ModList.get().getMods()) {
            if (modInfo.getDisplayName().startsWith(currentFilter)) {
                modResults.add(modInfo);
            }
        }
        itemResults = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item.getName(item.getDefaultInstance()).getString().startsWith(currentFilter)) {
                itemResults.add(item);
            }
        }
        this.resultsBox.updateResults(modResults, itemResults);
    }

    private void updateResultsHeight() {
        this.targetSearchResultsHeight = this.resultsBox.children().size() * ExDepotImageButton.BUTTON_HEIGHT;
        this.targetSearchResultsHeight = Math.min(this.targetSearchResultsHeight, getRealEstateHeight() - 2 * ExDepotImageButton.BUTTON_HEIGHT);
        this.targetSearchResultsHeight = Math.max(this.targetSearchResultsHeight, 0);
    }

    // From the bottom of the last button across the top of the screen, to the bottom of the screen,
    // is wide open space for widgets. This returns the height of that space.
    private int getRealEstateHeight() {
        return height - MIN_ELEMENT_SEPARATION * 3 - ExDepotImageButton.BUTTON_HEIGHT;
    }
}
