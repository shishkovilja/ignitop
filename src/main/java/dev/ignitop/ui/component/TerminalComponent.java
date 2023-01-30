package dev.ignitop.ui.component;

import java.io.PrintStream;

/**
 *
 */
@SuppressWarnings("UnnecessaryModifier")
public interface TerminalComponent {
    /**
     * Render component with a specified width. Components with a content will be shrinked if specified width
     * is less than component's content width. Some components can expand to a specified width.
     *
     * @param width Desired width.
     * @param out   Output stream for printing a component.
     */
    public void render(int width, PrintStream out);

    /**
     * Minimal width of a component, which does not lead to component's content shrinking.
     */
    public int contentWidth();
}
