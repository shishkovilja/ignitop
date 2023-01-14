package dev.ignitop.ui;

import org.fusesource.jansi.AnsiConsole;

import static java.lang.System.out;

/**
 *
 */
public class Terminal implements AutoCloseable {
    /** Default terminal width. */
    public static final int DEFAULT_TERMINAL_WIDTH = 80;

    /** Closed state marker. */
    private boolean closed;

    /**
     * Default constructor.
     */
    public Terminal() {
    // TODO: How we should handle null console?
    //    if (System.console() == null)
    //        throw new IllegalStateException("No suitable instance of console found. Windows command line or " +
    //            "linux terminal application must be user in order to run IgniTop.");

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

    /** {@inheritDoc} */
    @Override public void close() {
        if (!closed) {
            exitPrivateMode();

            closed = true;
        }
    }

    /**
     *
     */
    public int width() {
        int width = AnsiConsole.getTerminalWidth();

        return width > 0 ? width : DEFAULT_TERMINAL_WIDTH;
    }
}
