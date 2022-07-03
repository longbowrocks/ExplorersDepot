package bike.guyona.exdepot.client;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.client.particles.DepositingItemParticle;
import bike.guyona.exdepot.sounds.SoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.LinkedList;

import static bike.guyona.exdepot.sounds.SoundEvents.NUM_DEPOSIT_SOUNDS;

public class DepositItemsJuice {
    private final int TICKS_PER_DEPOSIT_SOUND = 3;
    private final Deque<Integer> itemsToDeposit = new LinkedList<>();
    private int lastDepositSoundTick;
    private int depositSoundIdx;

    public DepositItemsJuice() {
        lastDepositSoundTick = -1;
        depositSoundIdx = 0;
    }

    public void addDepositEvent(int itemId) {
        itemsToDeposit.add(itemId);
    }

    public void handleClientTick() {
        if (itemsToDeposit.size() > 0) {
            tryDoDepositJuice();
        } else {
            resetDepositSounds();
        }
    }

    private void tryDoDepositJuice() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            ExDepotMod.LOGGER.error("Client has no Steve. Perhaps client is on main menu?");
            return;
        }
        if (player.tickCount - lastDepositSoundTick < TICKS_PER_DEPOSIT_SOUND) {
            return;
        }
        ExDepotMod.LOGGER.info("Popping item: {}", itemsToDeposit.pop());
        doDepositSound(player, SoundEvents.DEPOSIT_SOUNDS.get(depositSoundIdx));
        doDepositVisual(new ItemStack(Items.CHEST), Minecraft.getInstance().player, new BlockPos(-1, -59, 2));
        depositSoundIdx = (depositSoundIdx + 1) % NUM_DEPOSIT_SOUNDS;
        lastDepositSoundTick = player.tickCount;
    }

    private void doDepositSound(@NotNull LocalPlayer player, @NotNull SoundEvent sound) {
        player.playSound(sound, 1, 1);
    }

    private void doDepositVisual(@NotNull ItemStack stack, @NotNull LocalPlayer source, @NotNull BlockPos target) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.particleEngine.add(
                new DepositingItemParticle(
                        stack,
                        minecraft.level,
                        source.getX(),
                        source.getY() + source.getEyeHeight(),
                        source.getZ(),
                        target
                )
        );
    }

    private void resetDepositSounds() {
        depositSoundIdx = 0;
    }
}
