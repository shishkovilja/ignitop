package dev.ignitop.ui.component.impl;

import dev.ignitop.ui.component.TerminalComponent;
import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.ansi;

/**
 *
 */
public class Title implements TerminalComponent {
    /** Text. */
    private final String text;

    /**
     * @param text Text.
     */
    public Title(String text) {
        this.text = text;
    }

    /** {@inheritDoc} */
    @Override public void render(int width) {
        String text0 = leftBracket() + text + rightBracket();

        int delta = Math.max(0, width - text.length());

        if (delta == 0)
            text0 = text.substring(0, width);

        int leftMarginSize = delta / 2;
        int rigthMarginSize = Math.max(0, width - leftMarginSize - text.length());

        System.out.println(ansi()
            .fg(fg())
            .bg(bg())
            .a(margin().repeat(leftMarginSize - 1))
            .a(text0)
            .a(margin().repeat(rigthMarginSize))
            .toString());
    }

    /**
     *
     */
    protected String leftBracket() {
        return "<";
    }

    /**
     *
     */
    protected String rightBracket() {
        return ">";
    }

    /**
     *
     */
    protected String margin() {
        // Horizontal line
        return "\u2500";
    }

    /**
     *
     */
    protected Ansi.Color fg() {
        return Ansi.Color.BLACK;
    }

    /**
     *
     */
    protected Ansi.Color bg() {
        return Ansi.Color.GREEN;
    }

    /** {@inheritDoc} */
    @Override public int contentWidth() {
        return text.length();
    }
}
