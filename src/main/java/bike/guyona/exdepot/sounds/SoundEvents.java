package bike.guyona.exdepot.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class SoundEvents {
    public static final int NUM_DEPOSIT_SOUNDS = 27;
    public static final List<RegistryObject<SoundEvent>> DEPOSIT_SOUNDS = new ArrayList<>();
    public static RegistryObject<SoundEvent> CONFIGURE_DEPOT_SUCCESS;
    public static RegistryObject<SoundEvent> CONFIGURE_DEPOT_MISS;
    public static RegistryObject<SoundEvent> CONFIGURE_DEPOT_FAIL;
    public static RegistryObject<SoundEvent> WAND_SWITCH;
}
