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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static dev.ignitop.ui.component.impl.Table.ASC_CHAR;
import static dev.ignitop.ui.component.impl.Table.CELLS_GAP;
import static dev.ignitop.util.TestUtils.DEC_SEP;
import static dev.ignitop.util.TestUtils.renderToString;
import static java.lang.System.lineSeparator;
import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
// TODO Refactor: method names, public modifiers, boiler plate in contentWidth_* (https://github.com/shishkovilja/ignitop/issues/55)
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
        checkTableWidths(new Table(List.of(WIDE_HEADER, NARROW_HEADER), ROWS),
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

        checkTableWidths(table, table.contentWidth(), List.of(col0, col1));
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
        checkTableWidths(new Table(List.of(NARROW_HEADER, NARROW_HEADER), ROWS),
            20,
            List.of(8, 8));
    }

    /**
     *
     */
    @Test
    void render_withContentShrinking_on1Char() {
        Table table = new Table(List.of(WIDE_HEADER, NARROW_HEADER), ROWS);

        checkTableWidths(table, table.contentWidth() - 1, List.of(WIDE_HEADER.length(), WIDEST_CONTENT.length() - 1));
    }

    /**
     *
     */
    @Test
    void render_withContentShrinking_on2Chars() {
        Table table = new Table(List.of(WIDE_HEADER, NARROW_HEADER), ROWS);

        checkTableWidths(table, table.contentWidth() - 2, List.of(WIDE_HEADER.length(), WIDEST_CONTENT.length() - 2));
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
        checkTableWidths(table, WIDE_HEADER.length() + NARROW_HEADER.length() - 1,
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
        checkTableWidths(table, WIDE_HEADER.length() + NARROW_HEADER.length() - 8,
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

        String renderedTable = renderToString(table, table.contentWidth());

        String renderedHdr = renderedTable.lines()
            .findFirst()
            .orElseThrow();

        String expHdr = ansi().fgBlack()
            .bg(Ansi.Color.BLUE)
            .a("Header")
            .a(ASC_CHAR)
            .a(" ")
            .reset()
            .toString() + ansi().fgBlack() // Looks like a bug in JANSI, reset does not work without #toString.
            .bgGreen()
            .a("null  ")
            .reset()
            .toString();

        assertEquals(expHdr, renderedHdr, "Unexpected header");
    }

    /**
     *
     */
    @Test
    void render_withNullTableElement() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"Cell01", null});

        Table table = new Table(List.of("Header1", "Header2"), rows);

        String renderedTable = renderToString(table, table.contentWidth());

        List<String> renderedTableLines = renderedTable.lines()
            .collect(Collectors.toList());

        String row0 = renderedTableLines.get(1);

        assertEquals("Cell01   null     ", row0);
    }

    /**
     *
     */
    @Test
    public void render_withDoubleValues() {
        List<String> hdr = List.of(WIDE_HEADER, WIDE_HEADER, WIDE_HEADER);

        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{4.003, 10.101, 0.91});

        Table table = new Table(hdr, rows);

        String[] renderedTableLines = renderToString(table, table.contentWidth()).split(lineSeparator());

        String[] cells = renderedTableLines[1].split(" +");

        assertEquals(3, cells.length, "Unexpected cells count in a row");

        assertEquals("4" + DEC_SEP + "0", cells[0]);
        assertEquals("10" + DEC_SEP + "1", cells[1]);
        assertEquals("0" + DEC_SEP + "9", cells[2]);
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

        checkTableWidths(table, table.contentWidth(), List.of(WIDE_HEADER.length(), NARROW_HEADER.length()));
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
     *
     */
    @Test
    void setSorting_firstColumn_ascending() {
        checkSetSorting(5, 0, true);
    }

    /**
     *
     */
    @Test
    void setSorting_secondColumn_descending() {
        checkSetSorting(5, 1, false);
    }

    /**
     *
     */
    @Test
    void setSorting_sortColumnIndex_correct() {
        Table table = new Table(List.of(WIDE_HEADER, WIDE_HEADER), rows(3));

        table.setSorting(1, false);

        assertEquals(1, table.sortingColumnIndex(), "Unxepected 'sortingColumnIndex'");
        assertFalse(table.ascendingSorting(), "Unxepected 'ascendingSorting'");
    }

    /**
     *
     */
    @Test
    void setSorting_negative_sortColumnIndex() {
        Table table = new Table(List.of(WIDE_HEADER, WIDE_HEADER), rows(3));

        table.setSorting(-1, false);

        assertEquals(0, table.sortingColumnIndex(), "Unxepected 'sortingColumnIndex'");
        assertTrue(table.ascendingSorting(), "Unxepected 'ascendingSorting'");
    }

    /**
     *
     */
    @Test
    void setSorting_outOfBunds_sortColumnIndex() {
        Table table = new Table(List.of(WIDE_HEADER, WIDE_HEADER), rows(3));

        table.setSorting(5, false);

        assertEquals(0, table.sortingColumnIndex(), "Unxepected 'sortingColumnIndex'");
        assertTrue(table.ascendingSorting(), "Unxepected 'ascendingSorting'");
    }

    /**
     * @param rowsCnt Rows count.
     */
    private static List<Object[]> rows(int rowsCnt) {
        return IntStream.range(0, rowsCnt)
            .mapToObj(i -> new Object[]{new Content(rowsCnt - i - 1, 0), new Content(i, 1)})
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
    private void checkTableWidths(Table table, int renderWidth, List<Integer> expColWidths) {
        String renderedTable = renderToString(table, renderWidth);

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
     * @param rowsWithHdr Rows with a header row.
     * @param colWidths Column widths.
     */
    private String tableWithWidths(List<List<String>> rowsWithHdr, List<Integer> colWidths) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rowsWithHdr.size(); i++) {
            Object[] rowCells = rowsWithHdr.get(i).toArray();

            for (int j = 0; j < colWidths.size(); j++) {
                String fmt = String.format("%%-%ds", colWidths.get(j) + CELLS_GAP);

                String formattedCell = String.format(fmt, rowCells[j]);

                // Coloring only for default sorting of table (by first column, ascending)
                if (i == 0) {
                    if (j == 0) {
                        String srtFmt = String.format("%%-%ds%%-%ds", colWidths.get(0), CELLS_GAP);

                        formattedCell = ansi().fgBlack()
                            .bg(Ansi.Color.BLUE)
                            .a(String.format(srtFmt, rowCells[0], ASC_CHAR))
                            .reset()
                            .toString();
                    }
                    else if (j == 1) {
                        formattedCell = ansi().fgBlack()
                            .bgGreen()
                            .a(String.format(fmt, rowCells[1]))
                            .reset() // Currently, it is a last column.
                            .toString();
                    }
                }

                sb.append(formattedCell);
            }

            sb.append(lineSeparator());
        }

        return sb.append("Total items: ")
            .append(rowsWithHdr.size() - 1)
            .append(lineSeparator())
            .toString();
    }

    /**
     * @param rowsCnt Rows count.
     * @param sortedCol Sorted column.
     * @param ascending Ascending sorting.
     */
    private void checkSetSorting(int rowsCnt, int sortedCol, boolean ascending) {
        List<Object[]> expRows = expectedSortedRows(rowsCnt, sortedCol, ascending);

        Table table = new Table(List.of(WIDE_HEADER, WIDE_HEADER), rows(rowsCnt));
        table.setSorting(sortedCol, ascending);

        Collection<Object[]> tableRows = table.rows();

        assertEquals(expRows.size(), tableRows.size(), "Unexpected rows count");

        Iterator<Object[]> rowsIter = tableRows.iterator();

        for (Object[] expRow : expRows)
            assertEquals(Arrays.toString(expRow), Arrays.toString(rowsIter.next()), "Rows should be equal");
    }

    /**
     * @param rowsCnt Rows count.
     * @param sortedCol Sorted col.
     * @param ascending Ascending.
     */
    private List<Object[]> expectedSortedRows(int rowsCnt, int sortedCol, boolean ascending) {
        List<Object[]> expRows = new ArrayList<>(rowsCnt);

        for (int i = 0; i < rowsCnt; i++) {
            Content ascCont0 = new Content(i, 0);
            Content ascCont1 = new Content(i, 1);

            Content descCont0 = new Content(rowsCnt - i - 1, 0);
            Content descCont1 = new Content(rowsCnt - i - 1, 1);

            Object[] row = sortedCol == 0 ?
                ascending ? new Object[]{ascCont0, descCont1} : new Object[]{descCont0, ascCont1} :
                ascending ? new Object[]{descCont0, ascCont1} : new Object[]{ascCont0, descCont1};

            expRows.add(row);
        }

        return expRows;
    }

    /**
     *
     */
    public static class Content implements Comparable<Content> {
        /** Row. */
        private final int row;

        /** Col. */
        private final int col;

        /**
         * @param row Row.
         * @param col Col.
         */
        public Content(int row, int col) {
            this.row = row;
            this.col = col;
        }

        /** {@inheritDoc} */
        @Override public int compareTo(@NotNull TableTest.Content o) {
            if (this == o)
                return 0;

            int rowsCompRes = Integer.compare(row, o.row);

            if (rowsCompRes != 0)
                return rowsCompRes;

            return Integer.compare(col, o.col);
        }

        /** {@inheritDoc} */
        @Override public boolean equals(Object o) {
            return o instanceof Content && compareTo((Content)o) == 0;
        }

        /** {@inheritDoc} */
        @Override public String toString() {
            return "Content [" + row + "," + col + ']';
        }
    }
}
