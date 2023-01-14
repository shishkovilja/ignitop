package dev.ignitop.ui.component.impl;

import org.fusesource.jansi.Ansi;

/**
 *
 */
public class Header extends Title {
    /**
     * @param text Text.
     */
    public Header(String text) {
        super(text);
    }

    /** {@inheritDoc} */
    @Override protected String leftBracket() {
        return "|";
    }

    /** {@inheritDoc} */
    @Override protected String rightBracket() {
        return "|";
    }

    /** {@inheritDoc} */
    @Override protected String margin() {
        return " ";
    }

    /** {@inheritDoc} */
    @Override protected Ansi.Color fg() {
        return Ansi.Color.WHITE;
    }

    /** {@inheritDoc} */
    @Override protected Ansi.Color bg() {
        return Ansi.Color.BLACK;
    }
}
