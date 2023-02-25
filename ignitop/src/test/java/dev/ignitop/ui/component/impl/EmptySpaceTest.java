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

import static dev.ignitop.ui.TerminalProvider.DEFAULT_TERMINAL_WIDTH;
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
