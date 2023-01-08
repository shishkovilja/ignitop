package dev.ignitop.ui.component;

/**
 *
 */
@SuppressWarnings("UnnecessaryModifier")
public interface TerminalComponent {
    /**
     * Render component with expanding or shrinking to a given width.
     *
     * @param width Desired width.
     */
    public void render(int width);

    /**
     *
     */
    public int contentWidth();
}
