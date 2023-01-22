package dev.ignitop.ui.component.impl;

import java.io.PrintStream;
import dev.ignitop.ui.TerminalUi;
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
    @Override public void render(int width, PrintStream out) {
        for (int i = 0; i < size; i++)
            out.println();
    }

    /** {@inheritDoc} */
    @Override public int contentWidth() {
        return TerminalUi.WHOLE_LINE;
    }
}
