package dev.ignitop.ui;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import dev.ignitop.ui.component.TerminalComponent;
import dev.ignitop.ui.updater.ScreenUpdater;

/**
 *
 */
public class TerminalUi {
    /** Size of a component, which take up whole line. */
    public static final int WHOLE_LINE = -1;

    /** Terminal. */
    private final Terminal terminal;

    /** Current screen updater. */
    private final AtomicReference<ScreenUpdater> updaterRef = new AtomicReference<>();

    /** UI width. */
    private int width;

    /**
     * @param terminal Terminal.
     */
    public TerminalUi(Terminal terminal) {
        this.terminal = terminal;

        width = terminal.width();
    }

    /**
     *
     */
    public void refresh() {
        Collection<TerminalComponent> components = updaterRef.get().components();

        width = terminal.width();

        int maxComponentWidth = components.stream()
            .mapToInt(TerminalComponent::contentWidth)
            .max()
            .orElse(width);

        terminal.eraseScreen();

        for (TerminalComponent component : components)
            component.render(Math.min(maxComponentWidth, width));
    }

    /**
     *
     */
    public boolean resized() {
        return width != terminal.width();
    }

    /**
     * @param updater New current screen updater.
     */
    public void updater(ScreenUpdater updater) {
        updaterRef.set(updater);
    }
}
