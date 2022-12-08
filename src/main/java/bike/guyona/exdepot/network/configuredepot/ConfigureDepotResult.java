package bike.guyona.exdepot.network.configuredepot;

import bike.guyona.exdepot.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.sounds.SoundEvents.DISPENSER_FAIL;

public enum ConfigureDepotResult {
    SUCCESS,
    WHAT_IS_THAT,
    NO_SELECTION;

    @NotNull
    public SoundEvent getSound() {
        return switch (this) {
            case SUCCESS -> SoundEvents.CONFIGURE_DEPOT_SUCCESS.get();
            case WHAT_IS_THAT -> SoundEvents.CONFIGURE_DEPOT_FAIL.get();
            case NO_SELECTION -> SoundEvents.CONFIGURE_DEPOT_MISS.get();
            default -> DISPENSER_FAIL;
        };
    }
}
