package bike.guyona.exdepot.api;

/**
 * This interface allows modders to make their own containers compatible with Explorer's Depot, or customize exactly
 * how their containers work with Explorer's Depot.
 *
 * Any BlockEntity that implements this interface, MUST have the ItemHandler capability.
 * Any BlockEntity that implements this interface, is compatible with Explorer's Depot.
 * The default implementation of each method is for vanilla, so it needs overriding.
 */
public interface IExDepotContainer {
}
