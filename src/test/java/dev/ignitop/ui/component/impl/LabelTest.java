/*
 * Copyright 2023 Ilya Shishkov (https://github.com/shishkovilja)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
