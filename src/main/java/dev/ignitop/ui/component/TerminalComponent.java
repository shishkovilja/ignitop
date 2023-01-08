package dev.ignitop.ui.component;

import dev.ignitop.ui.Terminal;

/**
 *
 */
@SuppressWarnings("UnnecessaryModifier")
public interface TerminalComponent {
    /**
     * @param terminal Terminal.
     * @param width Width.
     */
    public void render(Terminal terminal, int width);

    /**
     *
     */
    public int contentWidth();
}
