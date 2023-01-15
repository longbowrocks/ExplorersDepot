package bike.guyona.exdepot.client;

import bike.guyona.exdepot.ExDepotMod;
import bike.guyona.exdepot.client.particles.DepositingItemParticle;
import bike.guyona.exdepot.sounds.SoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static bike.guyona.exdepot.sounds.SoundEvents.NUM_DEPOSIT_SOUNDS;

public class DepositItemsJuice {
    private final int TICKS_PER_DEPOSIT_SOUND = 3;
    private final Deque<Pair<BlockPos, ItemStack>> itemsToDeposit = new LinkedList<>();
    private long ticksSinceLastDepositSound;
    private int depositSoundIdx;

    public DepositItemsJuice() {
        ticksSinceLastDepositSound = -1;
        depositSoundIdx = 0;
    }

    public void enqueueDepositEvent(Map<BlockPos, List<ItemStack>> sortingResults) {
        for (BlockPos depositLocation : sortingResults.keySet()) {
            for (ItemStack stack : sortingResults.get(depositLocation)) {
                itemsToDeposit.add(new ImmutablePair<>(depositLocation, stack));
            }
        }
    }

    public void handleClientTick() {
        if (itemsToDeposit.size() > 0) {
            tryDoDepositJuice();
        } else {
            resetDepositSounds();
        }
        ticksSinceLastDepositSound++;
    }

    private void tryDoDepositJuice() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            ExDepotMod.LOGGER.error("Client has no Steve. Perhaps client is on main menu?");
            return;
        }
        if (ticksSinceLastDepositSound < TICKS_PER_DEPOSIT_SOUND) {
            return;
        }
        ExDepotMod.LOGGER.info("Doing item deposit sound/visual");
        Pair<BlockPos, ItemStack> depositedStack = itemsToDeposit.pop();
        doDepositSound(player, SoundEvents.DEPOSIT_SOUNDS.get(depositSoundIdx).get());
        doDepositVisual(depositedStack.getRight(), Minecraft.getInstance().player, depositedStack.getLeft());
        depositSoundIdx = (depositSoundIdx + 1) % NUM_DEPOSIT_SOUNDS;
        ticksSinceLastDepositSound = -1;
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
