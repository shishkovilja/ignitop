package dev.ignitop.ui;

import java.io.PrintStream;
import org.fusesource.jansi.AnsiConsole;

/**
 *
 */
@SuppressWarnings("resource")
public class Terminal implements AutoCloseable {
    /** Default terminal width. */
    public static final int DEFAULT_TERMINAL_WIDTH = 80;

    /** Standard output print stream. */
    private final PrintStream out;

    /** Error output print stream. */
    private final PrintStream err;

    /** Closed state marker. */
    private boolean closed;

    /**
     * Default constructor.
     */
    public Terminal() {
//        if (System.console() == null)
//            throw new IllegalStateException("No suitable instance of console found. Windows command line or " +
//                "linux terminal application must be user in order to run IgniTop.");

        enterPrivateMode();

        out = AnsiConsole.out();
        err = AnsiConsole.err();
    }

    /**
     * Enter private mode with alternative buffer.
     */
    private void enterPrivateMode() {
        AnsiConsole.systemInstall();

        AnsiConsole.out().println("\033[?1049h");
        AnsiConsole.out().flush();
    }

    /**
     * Exit private mode, i.e. disable alternative buffer.
     */
    private void exitPrivateMode() {
        AnsiConsole.out().println("\033[?1049l");
        AnsiConsole.out().flush();

        AnsiConsole.systemUninstall();
    }

    /** {@inheritDoc} */
    @Override public void close() {
        if (!closed) {
            exitPrivateMode();

            closed = true;
        }
    }

    /**
     * @return Standard output print stream.
     */
    public PrintStream out() {
        return out;
    }

    /**
     * @return Error output print stream.
     */
    public PrintStream err() {
        return err;
    }

    /**
     *
     */
    public int width() {
        int width = AnsiConsole.getTerminalWidth();

        return width > 0 ? width : DEFAULT_TERMINAL_WIDTH;
    }
}