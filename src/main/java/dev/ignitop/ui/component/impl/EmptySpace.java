package dev.ignitop.ui.component.impl;

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
    @Override public void render(int width) {
        for (int i = 0; i < size; i++)
            System.out.println();
    }

    /** {@inheritDoc} */
    @Override public int contentWidth() {
        return TerminalUI.WHOLE_LINE;
    }
}
