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
import dev.ignitop.ui.component.TerminalComponent;
import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.ansi;

/**
 *
 */
public class Title implements TerminalComponent {
    /** Text. */
    private final String text;

    /**
     * @param text Text.
     */
    public Title(String text) {
        this.text = text;
    }

    /** {@inheritDoc} */
    @Override public void render(int width, PrintStream out) {
        String text0 = leftBracket() + text + rightBracket();

        int delta = Math.max(0, width - text0.length());

        if (delta == 0)
            text0 = text0.substring(0, width);

        int leftMarginSize = delta / 2;
        int rigthMarginSize = Math.max(0, delta - leftMarginSize);

        out.println(ansi()
            .fg(fg())
            .bg(bg())
            .a(margin().repeat(leftMarginSize))
            .bold()
            .a(text0)
            .boldOff()
            .a(margin().repeat(rigthMarginSize))
            .reset()
            .toString());
    }

    /**
     *
     */
    protected String leftBracket() {
        return "<";
    }

    /**
     *
     */
    protected String rightBracket() {
        return ">";
    }

    /**
     *
     */
    protected String margin() {
        return "─";
    }

    /**
     *
     */
    protected Ansi.Color fg() {
        return Ansi.Color.BLACK;
    }

    /**
     *
     */
    protected Ansi.Color bg() {
        return Ansi.Color.GREEN;
    }

    /** {@inheritDoc} */
    @Override public int contentWidth() {
        // Including left and right brackets
        return text.length() + leftBracket().length() + rightBracket().length();
    }
}
