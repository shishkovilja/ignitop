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
import java.util.Collections;
import java.util.List;
import dev.ignitop.ignite.IgniteHelper;
import dev.ignitop.ignite.system.SystemMetricsInformation;
import dev.ignitop.ui.component.TerminalComponent;
import dev.ignitop.ui.component.impl.Table;
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

        Collection<SystemMetricsInformation> sysMetrics = igniteHelper.systemMetrics();

        List<String> hdr = new ArrayList<>(List.of("ConsID", "HostNames", "CPU%", "LoadAvg", "GC_CPU%", "Heap%"));

        SystemMetricsInformation randomInfo = sysMetrics.iterator().next();

        // Sorting of data region usages by names is provided by SystemMetricsInformation
        randomInfo.dataRegionUsagesPercents()
            .keySet()
            .forEach(drName -> hdr.add("DataReg%:" + drName));

        hdr.add("DStorageGB");

        List<Object[]> rows = new ArrayList<>();

        for (SystemMetricsInformation info : sysMetrics)
            rows.add(toRow(info));

        components.add(new Table(hdr, rows));

        return Collections.unmodifiableList(components);
    }

    /**
     * Convert SystemMetricsInformation to a row of elements.
     *
     * @param info Info.
     */
    private Object[] toRow(SystemMetricsInformation info) {
        List<?> row0 = List.of(
            info.consistentId(),
            info.hostNames(),
            info.cpuLoadPercent(),
            info.loadAverage(),
            info.gcCpuLoadPercent(),
            info.heapUsagePercent());

        List<Object> row = new ArrayList<>(row0);

        row.addAll(info.dataRegionUsagesPercents().values());

        row.add(info.dataStorageSizeGigabytes());

        return row.toArray();
    }
}
