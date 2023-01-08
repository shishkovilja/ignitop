package dev.ignitop.ui.component.impl;

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
    @Override public void render(int width) {
        System.out.println(text.length() > width ? text.substring(0, width) : text);
    }

    /** {@inheritDoc} */
    @Override public int contentWidth() {
        return text.length();
    }
}
