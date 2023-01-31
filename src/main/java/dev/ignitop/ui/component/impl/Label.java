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

/**
 * Complex label class. Can be build phrase-by-phrase (whitespace separated). Each phrase can have own styling.
 */
public class Label implements TerminalComponent {
    /** Text. */
    private final String text;

    /**
     * @param text Text.
     */
    private Label(String text) {
        this.text = text;
    }

    /**
     * Add bold text.
     *
     * @param obj Object.
     */
    public static Label.Builder bold(Object obj) {
        return new Builder().bold(obj);
    }

    /**
     * Add ordinary text.
     *
     * @param obj Object.
     */
    public static Builder normal(Object obj) {
        return new Builder().normal(obj);
    }

    /**
     * Add underlined text.
     *
     * @param obj Object.
     */
    public static Builder underline(Object obj) {
        return new Builder().underline(obj);
    }

    /**
     * Add whitespaces.
     *
     * @param num Amount of whitespaces.
     */
    public static Builder spaces(int num) {
        return new Builder().spaces(num);
    }

    /**
     * Apply color.
     *
     * @param color Color.
     */
    public static Builder color(Ansi.Color color) {
        return new Builder().color(color);
    }

    /** {@inheritDoc} */
    @Override public void render(int width, PrintStream out) {
        out.println(text.length() > width ? text.substring(0, width) : text);
    }

    /** {@inheritDoc} */
    @Override public int contentWidth() {
        return text.length();
    }

    /**
     * Builder for label.
     */
    @SuppressWarnings("PublicInnerClass")
    public static final class Builder {
        /** Ansi. */
        private final Ansi ansi = Ansi.ansi();

        /** Emptiness marker. */
        private boolean needSpace;

        /** */
        private void addWhitespaceIfNecessary() {
            if (needSpace) {
                ansi.a(' ');

                needSpace = false;
            } else
                needSpace = true;
        }

        /** */
        public Label build() {
            return new Label(ansi.reset().toString());
        }

        /** */
        public Builder bold(Object obj) {
            addWhitespaceIfNecessary();

            ansi.bold()
                .a(String.valueOf(obj))
                .reset(); // Reset style and color

            return this;
        }

        /** */
        public Builder normal(Object obj) {
            addWhitespaceIfNecessary();

            ansi.a(String.valueOf(obj))
                .reset(); // Reset style and color

            return this;
        }

        /** */
        public Builder underline(Object obj) {
            addWhitespaceIfNecessary();

            ansi.a(Ansi.Attribute.UNDERLINE)
                .a(String.valueOf(obj))
                .reset(); // Reset style and color

            return this;
        }

        /** */
        public Builder spaces(int num) {
            ansi.a(" ".repeat(num));

            needSpace = false;

            return this;
        }

        /** */
        public Builder color(Ansi.Color color) {
            addWhitespaceIfNecessary();

            needSpace = false;

            ansi.fg(color);

            return this;
        }
    }
}
