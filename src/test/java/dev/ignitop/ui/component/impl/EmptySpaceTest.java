package dev.ignitop.ui.component.impl;

import dev.ignitop.util.TestUtils;
import org.junit.jupiter.api.Test;

import static dev.ignitop.ui.Terminal.DEFAULT_TERMINAL_WIDTH;
import static dev.ignitop.ui.TerminalUi.WHOLE_LINE;
import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class EmptySpaceTest {
    /**
     *
     */
    @Test
    void renderSingleEmptySpace() {
        String rendered = TestUtils.renderToString(new EmptySpace(1), DEFAULT_TERMINAL_WIDTH);

        assertEquals(lineSeparator(), rendered);
    }

    /**
     *
     */
    @Test
    void renderMultipleEmptySpace() {
        String rendered = TestUtils.renderToString(new EmptySpace(10), DEFAULT_TERMINAL_WIDTH);

        assertEquals(lineSeparator().repeat(10), rendered);
    }

    /**
     *
     */
    @Test
    void contentWidth() {
        assertEquals(WHOLE_LINE, new EmptySpace(1).contentWidth());
    }
}
