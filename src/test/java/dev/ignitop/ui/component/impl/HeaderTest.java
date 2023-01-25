package dev.ignitop.ui.component.impl;

import dev.ignitop.util.TestUtils;
import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.Test;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
    void render_withWidthLessThanContent() {
        fail("Unimplemeted");
    }

    /**
     *
     */
    @Test
    void contentWidth() {
        assertEquals(6, new Header("Header").contentWidth());
    }
}
