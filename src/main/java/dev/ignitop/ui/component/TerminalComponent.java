package dev.ignitop.ui.component;

import dev.ignitop.ui.Terminal;

/**
 *
 */
@SuppressWarnings("UnnecessaryModifier")
public interface TerminalComponent {
    /**
     * Render component in a given terminal. Component will be expanded or shrinked to a given size.
     *
     * @param terminal Terminal.
     * @param width Desired width.
     */
    public void render(Terminal terminal, int width);

    /**
     *
     */
    public int contentWidth();
}
