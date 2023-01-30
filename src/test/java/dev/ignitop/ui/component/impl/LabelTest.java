package dev.ignitop.ui.component.impl;

import java.util.function.Function;
import dev.ignitop.util.TestUtils;
import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static dev.ignitop.ui.Terminal.DEFAULT_TERMINAL_WIDTH;
import static java.lang.System.lineSeparator;
import static org.fusesource.jansi.Ansi.Attribute.UNDERLINE;
import static org.fusesource.jansi.Ansi.Color.BLUE;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 */
class LabelTest {
    /**
     *
     */
    @BeforeEach
    void setUp() {
    }

    /**
     *
     */
    @AfterEach
    void tearDown() {
    }

    /**
     *
     */
    @Test
    void singleBold() {
        check(Label::bold, s -> ansi().bold().a(s).reset());
    }

    /**
     *
     */
    @Test
    void singleNormal() {
        check(Label::normal, s -> ansi().a(s).reset());
    }

    /**
     *
     */
    @Test
    void singleUnderline() {
        check(Label::underline, s -> ansi().a(UNDERLINE).a(s).reset());
    }

    /**
     *
     */
    @Test
    void singleSpaces() {
        check(s -> Label.spaces(3), s -> ansi().a("   "));
    }

    /**
     *
     */
    @Test
    void singleColor() {
        check(s -> Label.color(RED).normal(s), s -> ansi().fgRed().a(s).reset());
    }

    /**
     *
     */
    @Test
    void multiple() {
        check(
            s -> Label.color(RED)
                .normal(s)
                .color(GREEN)
                .bold(s)
                .spaces(2)
                .color(BLUE)
                .underline(s),
            s -> ansi().fgRed()
                .a(s)
                .reset()
                .a(' ')
                .fgGreen()
                .bold()
                .a(s)
                .reset()
                .a("  ")
                .fgBlue()
                .a(UNDERLINE)
                .a(s)
                .reset()
        );
    }

    /**
     *
     */
    @Test
    @Disabled("https://github.com/shishkovilja/ignitop/issues/38")
    void multiple_widthLessThanContent() {
        fail("Unimplemented");
    }

    /**
     *
     */
    @Test
    @Disabled("https://github.com/shishkovilja/ignitop/issues/38")
    void contentWidth() {
        fail("Unimplemented");
    }

    /**
     * @param lblBuilderFunc Label builder function.
     * @param ansiBuilderFunc Ansi builder function.
     */
    private void check(Function<String, Label.Builder> lblBuilderFunc, Function<String, Ansi> ansiBuilderFunc) {
        String text = "text";

        String expectedText = ansiBuilderFunc.apply(text).reset().toString();

        Label lbl = lblBuilderFunc.apply(text).build();

        String renderedText;

        renderedText = TestUtils.renderToString(lbl, DEFAULT_TERMINAL_WIDTH);

        assertEquals(expectedText + lineSeparator(), renderedText);
        assertEquals(expectedText.length(), lbl.contentWidth());
    }
}
