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
import dev.ignitop.ui.component.TerminalComponent;
import dev.ignitop.ui.component.impl.Table;
import dev.ignitop.ui.updater.ScreenUpdater;

/**
 *
 */
public class TerminalUi {
    /** Default sorting column index. */
    public static final int DEFAULT_SORTING_COLUMN = 0;

    /** Size of a component, which take up whole line. */
    public static final int WHOLE_LINE = -1;

    /** TerminalProvider. */
    private final TerminalProvider terminalProvider;

    /** Current screen updater. */
    private ScreenUpdater updater;

    /** Components. */
    private Collection<TerminalComponent> components;

    /** UI width. */
    private int width;

    /** Sorting column index. */
    private int sortColIdx = DEFAULT_SORTING_COLUMN;

    /** Ascending sorting. */
    private boolean ascending = true;


    /**
     * @param terminalProvider TerminalProvider.
     */
    public TerminalUi(TerminalProvider terminalProvider) {
        this.terminalProvider = terminalProvider;

        width = terminalProvider.width();
    }

    /**
     * Update content and refresh screen.
     */
    public synchronized void updateContent() {
        if (updater == null)
            return;

        components = updater.updatedComponents();

        refresh();
    }

    /**
     * Refresh screen without content updating.
     */
    public synchronized void refresh() {
        width = terminalProvider.width();

        int maxComponentWidth = components.stream()
            .mapToInt(TerminalComponent::contentWidth)
            .max()
            .orElse(width);

        terminalProvider.eraseScreen();

        for (TerminalComponent component : components) {
            if (component instanceof Table)
                ((Table)component).setSorting(sortColIdx, ascending);
        }

        for (TerminalComponent component : components)
            component.render(Math.min(maxComponentWidth, width), terminalProvider.out());
    }

    /**
     * @param updater New current screen updater.
     */
    public synchronized void updater(ScreenUpdater updater) {
        this.updater = updater;

        updateContent();
    }

    /**
     * Set sorting column for screen tables. Duplicated set of same column reverses sorting order.
     *
     * @param sortColIdx Sorting column index.
     */
    public synchronized void setSortingColumn(int sortColIdx) {
        if (sortColIdx == this.sortColIdx)
            ascending = !ascending;

        this.sortColIdx = sortColIdx;

        refresh();
    }
}
