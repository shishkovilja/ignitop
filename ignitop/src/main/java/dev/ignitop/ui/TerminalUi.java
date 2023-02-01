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
            component.render(Math.min(maxComponentWidth, width), terminal.out());
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
