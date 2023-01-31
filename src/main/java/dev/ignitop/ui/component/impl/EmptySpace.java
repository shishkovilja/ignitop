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

import java.io.PrintStream;
import dev.ignitop.ui.TerminalUi;
import dev.ignitop.ui.component.TerminalComponent;

/**
 *
 */
public class EmptySpace implements TerminalComponent {
    /** Size. */
    private final int size;

    /**
     * @param size Size.
     */
    public EmptySpace(int size) {
        this.size = size;
    }

    /** {@inheritDoc} */
    @Override public void render(int width, PrintStream out) {
        for (int i = 0; i < size; i++)
            out.println();
    }

    /** {@inheritDoc} */
    @Override public int contentWidth() {
        return TerminalUi.WHOLE_LINE;
    }
}
