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

import org.fusesource.jansi.Ansi;

/**
 *
 */
public class Header extends Title {
    /**
     * @param text Text.
     */
    public Header(String text) {
        super(text);
    }

    /** {@inheritDoc} */
    @Override protected String leftBracket() {
        return "|";
    }

    /** {@inheritDoc} */
    @Override protected String rightBracket() {
        return "|";
    }

    /** {@inheritDoc} */
    @Override protected String margin() {
        return " ";
    }

    /** {@inheritDoc} */
    @Override protected Ansi.Color fg() {
        return Ansi.Color.WHITE;
    }

    /** {@inheritDoc} */
    @Override protected Ansi.Color bg() {
        return Ansi.Color.DEFAULT;
    }
}
