package bike.guyona.exdepot.sortingrules.mod;

import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.Charset;
import java.util.Optional;

public class ModSortingRule extends AbstractSortingRule {
    static final long serialVersionUID = 30;

    String modId;

    public ModSortingRule(String modId) {
        this.modId = modId;
    }

    public ModSortingRule(CompoundTag nbt, int version) {
        this.load(nbt, version);
    }

    @Override
    public void save(CompoundTag nbt) {
        nbt.putString("ModId", modId);
    }

    @Override
    public AbstractSortingRule load(CompoundTag nbt, int version) {
        modId = nbt.getString("ModId");
        return this;
    }

    @Override
    public int hashCode() {
        return modId.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ModSortingRule) {
            return ((ModSortingRule) other).modId.equals(modId);
        }
        return false;
    }

    @Override
    public Component getDisplayName() {
        Optional<? extends ModContainer> mod = ModList.get().getModContainerById(modId);
        return mod.map(
                modContainer -> new TranslatableComponent(modContainer.getModInfo().getDisplayName())
        ).orElse(
                new TranslatableComponent("exdepot.depot.name.notfound")
        );
    }

    @Override
    public void draw(int left, int top, float zLevel) {

    }

    @Override
    public Component getTypeDisplayName() {
        return new TranslatableComponent("exdepot.depot.type.mod");
    }

    @Override
    public byte[] toBytes() {
        return modId.getBytes();
    }

    public String getModId() {
        return modId;
    }
}
