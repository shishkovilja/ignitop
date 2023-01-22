package dev.ignitop.ui.component.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Function;
import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static java.lang.System.lineSeparator;
import static org.fusesource.jansi.Ansi.Attribute.UNDERLINE;
import static org.fusesource.jansi.Ansi.Attribute.UNDERLINE_OFF;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.*;

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
    void singleBold() throws Exception {
        check(Label::bold, s -> ansi().bold().a(s).boldOff().reset());
    }

    /**
     *
     */
    @Test
    void singleNormal() throws Exception {
        check(Label::normal, s -> ansi().a(s).reset());
    }

    /**
     *
     */
    @Test
    void singleUnderline() throws Exception {
        check(Label::underline, s -> ansi().a(UNDERLINE).a(s).a(UNDERLINE_OFF).reset());
    }

    /**
     *
     */
    @Test
    void singleSpaces() throws Exception {
        check(s -> Label.spaces(3), s -> ansi().a("   ").reset());
    }

    /**
     *
     */
    @Test
    void singleColor() throws Exception {
        check(s -> Label.color(RED).normal(s), s -> ansi().fgRed().a(s).reset());
    }

    /**
     *
     */
    @Test
    void multiple() throws IOException {
        check(
            s -> Label.color(RED)
                .normal(s)
                .bold(s)
                .spaces(2)
                .underline(s),
            s -> ansi().fgRed()
                .a(s)
                .a(' ')
                .bold().a(s).boldOff()
                .a("  ")
                .a(UNDERLINE).a(s).a(UNDERLINE_OFF)
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
     * @param lblBuilderFunc Label builder function.
     * @param ansiBuilderFunc Ansi builder function.
     */
    private void check(Function<String, Label.Builder> lblBuilderFunc,
        Function<String, Ansi> ansiBuilderFunc) throws IOException {
        String text = "text";

        String expectedText = ansiBuilderFunc.apply(text).toString();
        Label lbl = lblBuilderFunc.apply(text).build();

        String renderedText;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PrintStream out = new PrintStream(baos);

            lbl.render(80, out);

            out.flush();

            renderedText = baos.toString();
        }

        assertEquals(expectedText + lineSeparator(), renderedText);
        assertEquals(expectedText.length(), lbl.contentWidth());
    }
}
