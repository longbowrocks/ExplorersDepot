package bike.guyona.exdepot.sortingrules.mod;

import bike.guyona.exdepot.sortingrules.AbstractSortingRule;
import net.minecraft.nbt.CompoundTag;

import java.nio.charset.Charset;

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
    public String getDisplayName() {
        return null;
    }

    @Override
    public void draw(int left, int top, float zLevel) {

    }

    @Override
    public String getTypeDisplayName() {
        return "Mod";
    }

    @Override
    public byte[] toBytes() {
        return modId.getBytes();
    }
}
