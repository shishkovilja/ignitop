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

package dev.ignitop.ui.updater.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import dev.ignitop.ignite.IgniteHelper;
import dev.ignitop.ui.component.TerminalComponent;
import dev.ignitop.ui.updater.ScreenUpdater;

/**
 *
 */
public class SystemMetricsUpdater implements ScreenUpdater {
    /** Ignite helper. */
    private final IgniteHelper igniteHelper;

    /**
     * @param igniteHelper Ignite helper.
     */
    public SystemMetricsUpdater(IgniteHelper igniteHelper) {
        this.igniteHelper = igniteHelper;
    }

    /** {@inheritDoc} */
    @Override public Collection<TerminalComponent> components() {
        List<TerminalComponent> components = new ArrayList<>();

        igniteHelper.systemMetrics();

        return components;
    }
}
