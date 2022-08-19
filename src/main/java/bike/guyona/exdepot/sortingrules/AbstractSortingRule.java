package bike.guyona.exdepot.sortingrules;

import net.minecraft.nbt.CompoundTag;

public abstract class AbstractSortingRule {
    public abstract void save(CompoundTag nbt);
    public abstract AbstractSortingRule load(CompoundTag nbt, int version);

    /**
     * Must be overridden to be deterministic. Same data, same hashcode.
     * @return
     */
    @Override
    public abstract int hashCode();

    /**
     * Must be overridden to be deterministic. Same data, same object.
     * @return
     */
    @Override
    public abstract boolean equals(Object other);

    /**
     * Get a description of this rule in a human-readable format.
     * e.g. a rule that only matches items from the IndustrialCraft mod will return "IndustrialCraft".
     * @return human-readable description of this rule instance
     */
    public abstract String getDisplayName();

    /**
     * NOTE: should probably change this to getTexture or something, since this function collapses multiple concepts into a single function call.
     * Draw this rule onscreen at a particular point.
     * @param left
     * @param top
     * @param zLevel
     */
    public abstract void draw(int left, int top, float zLevel);

    /**
     * What rules of a particular type are called.
     * e.g. a list of rules that are based on the mod an item came from would have the type display name "Mod".
     * @return the human-readable name for this rule class
     */
    public abstract String getTypeDisplayName();

    /**
     * Encode this rule as a byte array
     * @return new byte array containing a reversible encoding of this rule.
     */
    public abstract byte[] toBytes();
}
