package dev.ignitop.ui.component.impl;

import dev.ignitop.util.TestUtils;
import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.Test;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class HeaderTest {
    /**
     *
     */
    @Test
    void renderNormal() {
        String expTitle = ansi()
            .fg(Ansi.Color.WHITE)
            .bgDefault()
            .a(" ".repeat(6))
            .bold()
            .a("|")
            .a("Header")
            .a("|")
            .boldOff()
            .a(" ".repeat(6))
            .reset()
            .toString() +
            System.lineSeparator();

        String renderedTitle = TestUtils.renderToString(new Header("Header"), 20);

        assertEquals(expTitle, renderedTitle);
    }

    /**
     *
     */
    @Test
    void render_withWidth1() {
        String expTitle = ansi()
            .fg(Ansi.Color.WHITE)
            .bgDefault()
            .a("")
            .bold()
            .a("|")
            .a("")
            .boldOff()
            .a("")
            .reset()
            .toString() +
            System.lineSeparator();

        String renderedTitle = TestUtils.renderToString(new Header("Header"), 1);

        assertEquals(expTitle, renderedTitle);
    }

    /**
     *
     */
    @Test
    void render_withWidth3() {
        String expTitle = ansi()
            .fg(Ansi.Color.WHITE)
            .bgDefault()
            .a("")
            .bold()
            .a("|")
            .a("He")
            .boldOff()
            .a("")
            .reset()
            .toString() +
            System.lineSeparator();

        String renderedTitle = TestUtils.renderToString(new Header("Header"), 3);

        assertEquals(expTitle, renderedTitle);
    }

    /**
     *
     */
    @Test
    void render_withContentWidth() {
        String expTitle = ansi()
            .fg(Ansi.Color.WHITE)
            .bgDefault()
            .a("")
            .bold()
            .a("|")
            .a("Header")
            .a("|")
            .boldOff()
            .a("")
            .reset()
            .toString() +
            System.lineSeparator();

        Header hdr = new Header("Header");

        String renderedTitle = TestUtils.renderToString(hdr, hdr.contentWidth());

        assertEquals(expTitle, renderedTitle);
    }

    /**
     *
     */
    @Test
    void contentWidth() {
        assertEquals(8, new Header("Header").contentWidth());
    }
}
