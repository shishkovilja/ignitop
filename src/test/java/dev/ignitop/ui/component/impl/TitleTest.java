package dev.ignitop.ui.component.impl;

import dev.ignitop.util.TestUtils;
import org.junit.jupiter.api.Test;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class TitleTest {
    /**
     *
     */
    @Test
    void renderNormal() {
        String expTitle = ansi()
            .fgBlack()
            .bgGreen()
            .a("─".repeat(6))
            .bold()
            .a("<")
            .a("Title")
            .a(">")
            .boldOff()
            .a("─".repeat(7))
            .reset()
            .toString() +
            System.lineSeparator();

        String renderedTitle = TestUtils.renderToString(new Title("Title"), 20);

        assertEquals(expTitle, renderedTitle);
    }

    /**
     *
     */
    @Test
    void render_withWidth1() {
        String expTitle = ansi()
            .fgBlack()
            .bgGreen()
            .a("")
            .bold()
            .a("<")
            .a("")
            .boldOff()
            .a("")
            .reset()
            .toString() +
            System.lineSeparator();

        String renderedTitle = TestUtils.renderToString(new Title("Title"), 1);

        assertEquals(expTitle, renderedTitle);
    }

    /**
     *
     */
    @Test
    void render_withWidth3() {
        String expTitle = ansi()
            .fgBlack()
            .bgGreen()
            .a("")
            .bold()
            .a("<")
            .a("Ti")
            .boldOff()
            .a("")
            .reset()
            .toString() +
            System.lineSeparator();

        String renderedTitle = TestUtils.renderToString(new Title("Title"), 3);

        assertEquals(expTitle, renderedTitle);
    }

    /**
     *
     */
    @Test
    void render_withContentWidth() {
        String expTitle = ansi()
            .fgBlack()
            .bgGreen()
            .a("")
            .bold()
            .a("<")
            .a("Title")
            .a(">")
            .boldOff()
            .a("")
            .reset()
            .toString() +
            System.lineSeparator();

        Title title = new Title("Title");

        String renderedTitle = TestUtils.renderToString(title, title.contentWidth());

        assertEquals(expTitle, renderedTitle);
    }

    /**
     *
     */
    @Test
    void contentWidth() {
        assertEquals(7, new Title("Title").contentWidth());
    }
}
