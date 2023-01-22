package dev.ignitop.ui.component;

import java.io.PrintStream;

/**
 *
 */
@SuppressWarnings("UnnecessaryModifier")
public interface TerminalComponent {
    /**
     * Render component with expanding or shrinking to a given width.
     *
     * @param width Desired width.
     * @param out   Output stream for printing a component.
     */
    public void render(int width, PrintStream out);

    /**
     *
     */
    public int contentWidth();
}
