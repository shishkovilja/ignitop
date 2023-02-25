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

import java.io.IOException;
import java.io.PrintStream;
import org.fusesource.jansi.AnsiConsole;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import static java.lang.System.out;

/**
 *
 */
public class TerminalProvider implements AutoCloseable {
    /** Default terminal width. */
    public static final int DEFAULT_TERMINAL_WIDTH = 80;

    /** Terminal. */
    private final Terminal terminal;

    /**
     * Default constructor.
     */
    public TerminalProvider() {
        enterPrivateMode();

        TerminalBuilder terminalBuilder = TerminalBuilder.builder();

        try {
            terminal = terminalBuilder.name("IgniTop")
                .build();

            terminal.enterRawMode();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Enter private mode with alternative buffer.
     */
    private void enterPrivateMode() {
        AnsiConsole.systemInstall();

        out.print("\033[?1049h");
        out.flush();

        hideCursor();
        eraseScreen();
    }

    /**
     * Exit private mode, i.e. disable alternative buffer.
     */
    private void exitPrivateMode() {
        showCursor();

        out.print("\033[?1049l");
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

    /**
     *
     */
    public NonBlockingReader reader() {
        return terminal.reader();
    }

    /** {@inheritDoc} */
    @Override public void close() {
        try {
            terminal.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            exitPrivateMode();
        }
    }
}
