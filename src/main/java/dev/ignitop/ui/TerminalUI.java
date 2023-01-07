package dev.ignitop.ui;

import java.util.ArrayList;
import java.util.List;
import dev.ignitop.ui.component.TerminalComponent;
import dev.ignitop.ui.component.impl.EmptySpace;

/**
 *
 */
public class TerminalUI {
    /** Empty space. */
    public static final EmptySpace EMPTY_SPACE = new EmptySpace(1);

    /** Terminal. */
    private final Terminal terminal;

    /** Components. */
    private List<TerminalComponent> components;

    /**
     * @param terminal Terminal.
     */
    public TerminalUI(Terminal terminal) {
        this.terminal = terminal;
    }

    /**
     *
     */
    public void emptyLine() {
        terminal.out().println();
    }

    /**
     * @param components Components.
     */
    public void setComponents(List<TerminalComponent> components) {
        this.components = new ArrayList<>(components);
    }

    /**
     *
     */
    public void refresh() {
        clearScreen();

        for (TerminalComponent component : components)
            component.renderWith(terminal);
    }

    /**
     *
     */
    public void clearScreen() {
        terminal.out().print("\033[H\033[2J");
        terminal.out().flush();
    }
}
