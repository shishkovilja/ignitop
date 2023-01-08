package dev.ignitop.ui.component.impl;

import dev.ignitop.ui.Terminal;
import dev.ignitop.ui.TerminalUI;
import dev.ignitop.ui.component.TerminalComponent;

/**
 *
 */
public class EmptySpace implements TerminalComponent {
    /** Size. */
    private final int size;

    /**
     * @param size Size.
     */
    public EmptySpace(int size) {
        this.size = size;
    }

    /** {@inheritDoc} */
    @Override public void render(Terminal terminal, int width) {
        for (int i = 0; i < size; i++)
            terminal.out().println();
    }

    /** {@inheritDoc} */
    @Override public int contentWidth() {
        return TerminalUI.WHOLE_LINE;
    }
}
