package bike.guyona.exdepot.sortingrules;

public abstract class AbstractSortingRule implements Comparable {
    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object other);

    public abstract String getDisplayName();

    public abstract void draw(int left, int top, int zLevel);

    public abstract String getTypeDisplayName();

    public abstract byte[] toBytes();
}
