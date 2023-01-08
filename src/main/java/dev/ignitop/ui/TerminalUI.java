package dev.ignitop.ui;

import java.util.ArrayList;
import java.util.List;
import dev.ignitop.ui.component.TerminalComponent;
import dev.ignitop.ui.component.impl.EmptySpace;

/**
 *
 */
public class TerminalUI {
    /** One line empty space. */
    public static final EmptySpace EMPTY_SPACE = new EmptySpace(1);

    /** Size of a component, which take up whole line. */
    public static final int WHOLE_LINE = -1;

    /** Terminal. */
    private final Terminal terminal;

    /** Components. */
    private List<TerminalComponent> components;

    /** UI width. */
    private int width;

    /**
     * @param terminal Terminal.
     */
    public TerminalUI(Terminal terminal) {
        this.terminal = terminal;

        width = terminal.width();
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
        terminal.eraseScreen();

        width = terminal.width();

        for (TerminalComponent component : components)
            component.render(width);
    }

    /**
     *
     */
    public boolean resized() {
        return width != terminal.width();
    }
}
