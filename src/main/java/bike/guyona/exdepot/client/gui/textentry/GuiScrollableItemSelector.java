package bike.guyona.exdepot.client.gui.textentry;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiScrollableItemSelector extends AbstractWidget implements Widget, GuiEventListener {
    private final EditBox text;
    public GuiScrollableItemSelector(Font font, int x, int y, int width, int height, Component prompt) {
        super(x, y, width, height, prompt);
        text = new EditBox(font, x, y, width, height, prompt);
    }

    public void tick() {
        text.tick();
    }

    @Override
    public boolean keyPressed(int key, int mouseX, int mouseY) {
        return text.keyPressed(key, mouseX, mouseY);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicksProbably) {
        text.render(poseStack, mouseX, mouseY, partialTicksProbably);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrator) {
        text.updateNarration(narrator);
    }
}
