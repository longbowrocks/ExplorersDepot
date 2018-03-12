package bike.guyona.exdepot.sortingrules;

public abstract class AbstractSortingRule {
    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object other);

    // similar to equals(), but also allows true for comparisons against other object types if they represent the thing
    // this sorting rule should be compared to
    public abstract boolean matches(Object thing);

    public abstract String getDisplayName();

    public abstract void draw(int left, int top, float zLevel);

    public abstract String getTypeDisplayName();

    public abstract byte[] toBytes();
}
