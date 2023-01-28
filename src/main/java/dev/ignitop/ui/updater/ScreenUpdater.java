package dev.ignitop.ui.updater;

import java.util.Collection;
import dev.ignitop.ui.component.TerminalComponent;

/**
 *
 */
// TODO: Migrate to Supplier<Collection<Components>>.
@SuppressWarnings("UnnecessaryModifier")
public interface ScreenUpdater {
    /**
     *
     */
    public Collection<TerminalComponent> components();
}
