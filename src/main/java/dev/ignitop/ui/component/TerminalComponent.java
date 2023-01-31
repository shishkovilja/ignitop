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

package dev.ignitop.ui.component;

import java.io.PrintStream;

/**
 *
 */
@SuppressWarnings("UnnecessaryModifier")
public interface TerminalComponent {
    /**
     * Render component with a specified width. Components with a content will be shrinked if specified width
     * is less than component's content width. Some components can expand to a specified width.
     *
     * @param width Desired width.
     * @param out   Output stream for printing a component.
     */
    public void render(int width, PrintStream out);

    /**
     * Minimal width of a component, which does not lead to component's content shrinking.
     */
    public int contentWidth();
}
