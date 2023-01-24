package dev.ignitop.ui.component.impl;

import dev.ignitop.util.TestUtils;
import org.junit.jupiter.api.Test;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
    void render_withWidthLessThanContent() {
        fail("Unimplemeted");
    }

    /**
     *
     */
    @Test
    void contentWidth() {
        assertEquals(5, new Title("Title").contentWidth());
    }
}
