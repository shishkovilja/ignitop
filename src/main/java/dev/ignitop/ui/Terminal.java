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
