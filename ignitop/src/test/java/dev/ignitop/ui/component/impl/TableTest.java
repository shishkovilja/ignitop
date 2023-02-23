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

package dev.ignitop.ui.component.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import dev.ignitop.util.TestUtils;
import org.junit.jupiter.api.Test;

import static java.lang.System.lineSeparator;
import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
// TODO Add test for double values.
class TableTest {
    /** Wide header. */
    public static final String WIDE_HEADER = "Very very very wide header";

    /** Narrow header. */
    public static final String NARROW_HEADER = "Narrhdr";

    /** Test table rows count. */
    public static final int ROWS_COUNT = 25;

    /** Rows. */
    public static final List<Object[]> ROWS = rows(ROWS_COUNT);

    /** Widest content. It is narrower than {@code WIDE_HEADER}, but wider than {@code NARROW_HEADER}. */
    public static final String WIDEST_CONTENT = "Content [" + (ROWS_COUNT - 1) + ",1]";

    /**
     *
     */
    @Test
    void render_withTableExpanding() {
        // Calculating by widest element:
        // "Very very very wide header  Content [24,1]  "
        // Col1: 26 + 2 (gap) = 28      Col2: 14 + 2 (gap) = 16
        // Delta = 80 - (28 + 16) = 36
        // Col1: 28 * 36 / 44 -> 28 expand on 22 -> 50 (36 - 22 = 14 of delta left) -> minus 2 gap -> 48
        // Col2: 16 * 36 / 44 -> 16 expand on 13 -> 29 (1 of delta left) -> extra expand on 1 -> 30 -> minus 2 gap -> 28
        checkTable(new Table(List.of(WIDE_HEADER, NARROW_HEADER), ROWS),
            80,
            List.of(48, 28));
    }

    /**
     *
     */
    @Test
    void render_withContentWidth() {
        int col0 = WIDE_HEADER.length();
        int col1 = WIDEST_CONTENT.length();

        Table table = new Table(List.of(WIDE_HEADER, NARROW_HEADER), ROWS);

        checkTable(table, table.contentWidth(), List.of(col0, col1));
    }

    /**
     *
     */
    @Test
    void render_withContentShrinking() {
        // Calculating by widest element:
        // "Content [24,1]  Content [24,1]  "
        // Col1: 14 + 2 (gap) = 16      Col2: 14 + 2 (gap) = 16
        // Delta = 20 - (16 + 16) = -12
        // Col1: -12 * 16 / 32 -> 16 shrink on 6 -> 10 (-12 - (-6) = -6 of delta left) -> minus 2 gap -> 8
        // Col2: -12 * 16 / 32 -> 16 shrink on 6 -> 10 (0 of delta left) -> minus 2 gap -> 8
        checkTable(new Table(List.of(NARROW_HEADER, NARROW_HEADER), ROWS),
            20,
            List.of(8, 8));
    }

    /**
     *
     */
    @Test
    void render_withContentShrinking_on1Char() {
        Table table = new Table(List.of(WIDE_HEADER, NARROW_HEADER), ROWS);

        checkTable(table, table.contentWidth() - 1, List.of(WIDE_HEADER.length(), WIDEST_CONTENT.length() - 1));
    }

    /**
     *
     */
    @Test
    void render_withContentShrinking_on2Chars() {
        Table table = new Table(List.of(WIDE_HEADER, NARROW_HEADER), ROWS);

        checkTable(table, table.contentWidth() - 2, List.of(WIDE_HEADER.length(), WIDEST_CONTENT.length() - 2));
    }

    /**
     *
     */
    @Test
    void render_whenRenderWidth_isLessThan_totalHeadersWidth_on1() {
        Table table = new Table(List.of(WIDE_HEADER, NARROW_HEADER), ROWS);

        // Calculating by widest element:
        // "Very very very wide header  Content [24,1]  "
        // Col1: 26 + 2 (gap) = 28      Col2: 14 + 2 (gap) = 16
        // Delta = (26 + 7 - 1) - (28 + 16) = -12
        // Col1: -12 * 28 / 44 -> 28 shrink on 7 -> 21 (-12 - (-7) = -5 of delta left) -> minus 2 gap -> 19
        // Col2: -12 * 16 / 44 -> 16 shrink on 4 -> 12 (-1 of delta left) -> extra shrink on 1 -> 11 -> minus 2 gap -> 9
        checkTable(table, WIDE_HEADER.length() + NARROW_HEADER.length() - 1,
            List.of(19, 9));
    }

    /**
     *
     */
    @Test
    void render_whenRenderWidth_isLessThan_totalHeadersWidth_on8() {
        Table table = new Table(List.of(WIDE_HEADER, NARROW_HEADER), ROWS);

        // Calculating by widest element:
        // "Very very very wide header  Content [24,1]  "
        // Col1: 26 + 2 (gap) = 28      Col2: 14 + 2 (gap) = 16
        // Delta = (26 + 7 - 8) - (28 + 16) = -19
        // Col1: -19 * 28 / 44 -> 28 shrink on 12 -> 16 (-19 - (-12) = -7 of delta left) -> minus 2 gap -> 14
        // Col2: -19 * 16 / 44 -> 16 shrink on 6 -> 10 (-1 of delta left) -> extra shrink on 1 -> 9 -> minus 2 gap -> 7
        checkTable(table, WIDE_HEADER.length() + NARROW_HEADER.length() - 8,
            List.of(14, 7));
    }

    /**
     *
     */
    @Test
    void render_nullHeaderElement() {
        ArrayList<String> hdr = new ArrayList<>();
        hdr.add("Header");
        hdr.add(null);

        Table table = new Table(hdr, List.of());

        String renderedTable = TestUtils.renderToString(table, table.contentWidth());

        String renderedHdr = renderedTable.lines()
            .findFirst()
            .orElseThrow();

        assertTrue(renderedHdr.contains("Header  null  "));
    }

    /**
     *
     */
    @Test
    void render_withNullTableElement() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"Cell01", null});

        Table table = new Table(List.of("Header1", "Header2"), rows);

        String renderedTable = TestUtils.renderToString(table, table.contentWidth());

        List<String> renderedTableLines = renderedTable.lines()
            .collect(Collectors.toList());

        String row0 = renderedTableLines.get(1);

        assertEquals("Cell01   null     ", row0);
    }

    /**
     *
     */
    @Test
    void tableCreate_withEmptyHeader_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new Table(List.of(), ROWS),
            "Table columns headers list must not be empty");
    }

    /**
     *
     */
    @Test
    void render_headerAndRows_sizesMismatch() {
        assertThrows(IllegalArgumentException.class,
            () -> new Table(
                List.of("Header0", "Header1"),
                List.of(new Object[]{"Cell00", "Cell01"}, new Object[]{"Cell10", "Cell11", "Cell12"})),
            "Row elements count does not correspond header elements count: [rowSize=3, hdrSize=2]");
    }

    /**
     *
     */
    @Test
    void render_withEmptyRows() {
        Table table = new Table(List.of(WIDE_HEADER, NARROW_HEADER), List.of());

        checkTable(table, table.contentWidth(), List.of(WIDE_HEADER.length(), NARROW_HEADER.length()));
    }

    /**
     *
     */
    @Test
    void contentWidth_withContentNarrower_thanHeader() {
        String narrowHdr = NARROW_HEADER;

        List<String> hdr = List.of(narrowHdr, narrowHdr);

        List<Object[]> rows = rows(5);

        assertEquals(("Content [x,y]".length() + 2) * 2,
            new Table(hdr, rows).contentWidth());
    }

    /**
     *
     */
    @Test
    void contentWidth_withContentWider_thanHeader() {
        String wideHdr = WIDE_HEADER;

        List<String> hdr = List.of(wideHdr, wideHdr);

        List<Object[]> rows = rows(5);

        assertEquals((wideHdr.length() + 2) * 2, new Table(hdr, rows).contentWidth());
    }

    /**
     * @param rowsCnt Rows count.
     */
    private static List<Object[]> rows(int rowsCnt) {
        return IntStream.range(0, rowsCnt)
            .mapToObj(i -> new Object[]{"Content [" + i + ",1]", "Content [" + i + ",2]"})
            .collect(Collectors.toList());
    }

    /**
     * Check,that rendering of a table with a specified width will be performed with the expected column widths.
     * Expected (not rendered) table shrinking to the expected column widths is performed in this method.
     *
     * @param table Table.
     * @param renderWidth Render width.
     * @param expColWidths Expected column widths.
     */
    private void checkTable(Table table, int renderWidth, List<Integer> expColWidths) {
        String renderedTable = TestUtils.renderToString(table, renderWidth);

        // Header + rows + total items line.
        assertEquals(table.rows().size() + 2, renderedTable.lines().count());

        ArrayList<List<String>> rowsWithHdr = new ArrayList<>();

        rowsWithHdr.add(new ArrayList<>(table.header()));

        for (Object[] row : table.rows()) {
            rowsWithHdr.add(Arrays.stream(row)
                .map(String::valueOf)
                .collect(Collectors.toList()));
        }

        // Shrink if neccessary
        for (int i = 0; i < rowsWithHdr.size(); i++) {
            for (int j = 0; j < rowsWithHdr.get(i).size(); j++) {
                String cell = rowsWithHdr.get(i).get(j);

                if (cell.length() > expColWidths.get(j))
                    rowsWithHdr.get(i).set(j, cell.substring(0, expColWidths.get(j)));
            }
        }

        String expTable = tableWithWidths(rowsWithHdr, expColWidths);

        assertEquals(expTable, renderedTable);
    }

    /**
     * Output table with specified widths.
     *
     * @param rowsWithHdr Rows with a header.
     * @param colWidths Column widths.
     */
    private String tableWithWidths(List<List<String>> rowsWithHdr, List<Integer> colWidths) {
        int colGap = 2;

        String rowFormat = String.format(
            "%%-%ds".repeat(colWidths.size()),
            colWidths.stream()
                .map(i -> i + colGap)
                .toArray());

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rowsWithHdr.size(); i++) {
            String formattedStr = String.format(rowFormat, rowsWithHdr.get(i).toArray());

            if (i == 0) {
                sb.append(ansi().fgBlack()
                    .bgGreen()
                    .a(formattedStr)
                    .reset()
                    .toString());
            }
            else
                sb.append(formattedStr);

            sb.append(lineSeparator());
        }

        return sb.append("Total items: ")
            .append(rowsWithHdr.size() - 1)
            .append(lineSeparator())
            .toString();
    }
}
