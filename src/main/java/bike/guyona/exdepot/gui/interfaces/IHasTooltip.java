package bike.guyona.exdepot.gui.interfaces;

public interface IHasTooltip {
    String getTooltip();

    String getLongTooltip();

    void drawTooltip(int x, int y, boolean drawLong);
}
