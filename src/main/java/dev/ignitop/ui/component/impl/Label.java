package dev.ignitop.ui.component.impl;

import dev.ignitop.ui.Terminal;
import dev.ignitop.ui.component.TerminalComponent;

/**
 *
 */
public class Label implements TerminalComponent {
    /** Text. */
    private final String text;

    /**
     * @param text Text.
     */
    public Label(String text) {
        this.text = text;
    }

    /** {@inheritDoc} */
    @Override public void renderWith(Terminal terminal) {
        terminal.out().println(text);
    }
}
