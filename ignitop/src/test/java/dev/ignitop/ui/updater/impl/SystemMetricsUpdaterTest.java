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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import dev.ignitop.ignite.IgniteHelper;
import dev.ignitop.ignite.system.SystemMetricsInformation;
import dev.ignitop.ui.component.TerminalComponent;
import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.ignitop.ui.component.impl.Table.ASC_CHAR;
import static dev.ignitop.util.TestUtils.DEC_SEP;
import static dev.ignitop.util.TestUtils.renderToString;
import static java.lang.System.lineSeparator;
import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 *
 */
class SystemMetricsUpdaterTest {
    /** Ignite helper. */
    private IgniteHelper igniteHelper;

    /**
     *
     */
    @BeforeEach
    void setUp() {
        igniteHelper = Mockito.mock(IgniteHelper.class);
    }

    /**
     *
     */
    @Test
    void components_with_OneNode_OneDataRegion() {
        doTest(1, 1);
    }

    /**
     *
     */
    @Test
    void components_with_ThreeNodes_FourDataRegions() {
        doTest(3, 4);
    }

    /**
     * @param nodesCnt Nodes count.
     * @param drCnt Data regions count.
     */
    private void doTest(int nodesCnt, int drCnt) {
        List<SystemMetricsInformation> infos = IntStream.range(0, nodesCnt)
            .mapToObj(i -> systemInformation(0, drCnt))
            .collect(Collectors.toList());

        List<List<String>> expTable = expectedTable(nodesCnt, drCnt);

        when(igniteHelper.systemMetrics()).thenReturn(infos);

        Iterator<TerminalComponent> compIter = new SystemMetricsUpdater(igniteHelper).components()
            .iterator();

        assertTrue(renderToString(compIter.next(), 400).contains("System metrics"), "Unexpected title");
        assertEquals(lineSeparator(), renderToString(compIter.next(), 400), "Unexpected empty space");

        String renderedTable = renderToString(compIter.next(), 400);

        List<List<String>> renderedCells = renderedTable.lines()
            .map(s -> Arrays.asList(s.split("\\s+")))
            .collect(Collectors.toList());

        Iterator<List<String>> expIter = expTable.iterator();
        Iterator<List<String>> renderedIter = renderedCells.iterator();

        while (expIter.hasNext()) {
            List<String> expRow = expIter.next();
            List<String> row = renderedIter.next();

            assertIterableEquals(expRow, row, "Unexpected tables row");
        }

        // "Dirty hack" because of rows splitting by whitespace.
        String totalStr = String.join(" ", renderedIter.next());

        assertTrue(totalStr.contains("Total items: " + nodesCnt), "Unexpected 'total' row");
        assertFalse(renderedIter.hasNext(), "Unexpected elements in rendered table");
    }

    /**
     * @param idx Index.
     * @param drCnt Dr count.
     */
    private SystemMetricsInformation systemInformation(int idx, int drCnt) {
        Map<String, Double> drPercents = new HashMap<>();

        for (int i = 0; i < drCnt; i++)
            drPercents.put("test" + i, 20.33);

        return new SystemMetricsInformation(
            "node" + idx,
            List.of("host" + idx + "0", "host" + idx + "1"),
            52.28,
            2.57,
            10.17,
            20.673,
            drPercents,
            70.701);
    }

    /**
     * @param nodesCnt Nodes count.
     * @param drCnt Data regions count.
     */
    private List<List<String>> expectedTable(int nodesCnt, int drCnt) {
        List<String> expHdr = expectedHeaderRow(drCnt);

        List<List<String>> expRows = expectedRows(nodesCnt, drCnt);

        List<List<String>> expTable = new ArrayList<>();
        expTable.add(expHdr);
        expTable.addAll(expRows);

        return expTable;
    }

    /**
     * @param drCnt Data regions count.
     */
    private List<String> expectedHeaderRow(int drCnt) {
        String consIdAnsiStr = ansi().fgBlack()
            .bg(Ansi.Color.BLUE)
            .a("ConsID")
            .a(ASC_CHAR)
            .toString();

        String hostNames = ansi().reset().toString() +
            ansi().fgBlack()
                .bgGreen()
                .a("HostNames")
                .toString();

        List<String> hdr0 = List.of(consIdAnsiStr, hostNames, "CPU%", "LoadAvg", "GC_CPU%", "Heap%");

        List<String> drRegsHdr = IntStream.range(0, drCnt)
            .mapToObj(i -> "DataReg%:test" + i)
            .collect(Collectors.toList());

        List<String> expHdr = new ArrayList<>(hdr0);

        expHdr.addAll(drRegsHdr);
        expHdr.addAll(List.of("DStorageGB", ansi().reset().toString()));

        return expHdr;
    }

    /**
     * @param nodesCnt Nodes count.
     * @param drCnt Data regions count.
     */
    private List<List<String>> expectedRows(int nodesCnt, int drCnt) {
        List<String> expRowPref = List.of(
            "node0",
            "[host00,", // "Dirty hack" because of rows splitting by whitespace.
            "host01]",
            "52" + DEC_SEP + "3",
            "2" + DEC_SEP + "6",
            "10" + DEC_SEP + "2",
            "20" + DEC_SEP + "7");

        List<List<String>> expRows = new ArrayList<>(nodesCnt);

        for (int i = 0; i < nodesCnt; i++) {
            List<String> row0 = new ArrayList<>(expRowPref);

            List<String> drPercents = IntStream.range(0, drCnt)
                .mapToObj(j -> "20" + DEC_SEP + "3")
                .collect(Collectors.toList());

            row0.addAll(drPercents);

            row0.add("70" + DEC_SEP + "7");

            expRows.add(row0);
        }
        return expRows;
    }

}
