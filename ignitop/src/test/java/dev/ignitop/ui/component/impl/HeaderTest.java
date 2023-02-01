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
        String expHdr = ansi()
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

        String renderedHdr = TestUtils.renderToString(new Header("Header"), 20);

        assertEquals(expHdr, renderedHdr);
    }

    /**
     *
     */
    @Test
    void render_withWidth1() {
        String expHdr = ansi()
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

        String renderedHdr = TestUtils.renderToString(new Header("Header"), 1);

        assertEquals(expHdr, renderedHdr);
    }

    /**
     *
     */
    @Test
    void render_withWidth3() {
        String expHdr = ansi()
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

        String renderedHdr = TestUtils.renderToString(new Header("Header"), 3);

        assertEquals(expHdr, renderedHdr);
    }

    /**
     *
     */
    @Test
    void render_withContentWidth() {
        String expHdr = ansi()
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

        String renderedHdr = TestUtils.renderToString(hdr, hdr.contentWidth());

        assertEquals(expHdr, renderedHdr);
    }

    /**
     *
     */
    @Test
    void contentWidth() {
        assertEquals(8, new Header("Header").contentWidth());
    }
}
