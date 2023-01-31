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
