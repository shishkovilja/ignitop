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

package dev.ignitop.ui;

import java.io.PrintStream;
import org.fusesource.jansi.AnsiConsole;

import static java.lang.System.out;

/**
 *
 */
public class Terminal implements AutoCloseable {
    /** Default terminal width. */
    public static final int DEFAULT_TERMINAL_WIDTH = 80;

    /** Closed state marker. */
    private volatile boolean closed;

    /**
     * Default constructor.
     */
    public Terminal() {
        enterPrivateMode();
    }

    /**
     * Enter private mode with alternative buffer.
     */
    private void enterPrivateMode() {
        AnsiConsole.systemInstall();

        out.println("\033[?1049h");
        out.flush();

        hideCursor();
        eraseScreen();
    }

    /**
     * Exit private mode, i.e. disable alternative buffer.
     */
    private void exitPrivateMode() {
        eraseScreen();
        showCursor();

        out.println("\033[?1049l");
        out.flush();

        AnsiConsole.systemUninstall();
    }

    /**
     *
     */
    private void hideCursor() {
        out.print("\033[?25l");
        out.flush();
    }

    /**
     *
     */
    private void showCursor() {
        out.print("\033[?25h");
        out.flush();
    }

    /**
     *
     */
    public void eraseScreen() {
        out.print("\033[H\033[2J");
        out.flush();
    }

    /**
     *
     */
    public int width() {
        int width = AnsiConsole.getTerminalWidth();

        return width > 0 ? width : DEFAULT_TERMINAL_WIDTH;
    }

    /**
     *
     */
    public PrintStream out() {
        return out;
    }

    /** {@inheritDoc} */
    @Override public void close() {
        if (!closed) {
            exitPrivateMode();

            closed = true;
        }
    }
}
