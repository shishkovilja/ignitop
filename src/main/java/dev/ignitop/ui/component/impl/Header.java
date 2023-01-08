package dev.ignitop.ui.component.impl;

import dev.ignitop.ui.component.TerminalComponent;
import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.ansi;

/**
 *
 */
public class Header implements TerminalComponent {
    /** Horizontal line. */
    public static final String HORIZONTAL_LINE = "\u2500";

    /** Text. */
    private final String text;

    /**
     * @param text Text.
     */
    public Header(String text) {
        this.text = text;
    }

    /** {@inheritDoc} */
    @Override public void render(int width) {
        String text0 = text;

        int delta = Math.max(0, width - text.length());

        if (delta == 0)
            text0 = text.substring(0, width);

        int leftMarginSize = delta / 2;
        int rigthMarginSize = Math.max(0, width - leftMarginSize - text.length());

        System.out.println(ansi()
                .a(HORIZONTAL_LINE.repeat(leftMarginSize))
                .fgBlack()
                .bg(Ansi.Color.WHITE)
                .a(text0)
                .reset()
                .a(HORIZONTAL_LINE.repeat(rigthMarginSize))
                .toString());
    }

    /** {@inheritDoc} */
    @Override public int contentWidth() {
        return text.length();
    }
}
