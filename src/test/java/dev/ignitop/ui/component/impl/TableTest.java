package dev.ignitop.ui.component.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import dev.ignitop.util.TestUtils;
import org.junit.jupiter.api.Test;

import static java.lang.System.lineSeparator;
import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 */
class TableTest {
    /** Wide header. */
    public static final String WIDE_HEADER = "Very very very wide header";

    /** Narrow header. */
    public static final String NARROW_HEADER = "Narrhdr";

    /** Test table rows count. */
    public static final int ROWS_COUNT = 25;

    /** Widest content. It is narrower than {@code WIDE_HEADER}, but wider than {@code NARROW_HEADER} */
    public static final String WIDEST_CONTENT = "Content [" + (ROWS_COUNT - 1) + ",1]";

    /**
     *
     */
    @Test
    void render_withExpanding() {
        // Calculating by widest element:
        // "Very very very wide header  Content [24,1]  "
        // Col1: 26 + 2 (gap) = 28      Col2: 14 + 2 (gap) = 16
        // Delta = 80 - (28 + 16) = 36
        // Col1: 28 * 36 / 44 -> expand on 22 -> 50 (36 - 22 = 14 of delta left) -> minus 2 gap -> 48
        // Col2: 16 * 36 / 44 -> expand on 13 -> 29 (1 of delta left) -> 30 -> minus 2 gap -> 28
        checkTable(new Table(List.of(WIDE_HEADER, NARROW_HEADER), rows(ROWS_COUNT)),
            80,
            List.of(48, 28));
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
        // Col1: -12 * 16 / 32 -> shrink on 6 -> 10 (-12 - (-6) = -6 of delta left) -> minus 2 gap -> 8
        // Col2: -12 * 16 / 32 -> shrink on 6 -> 10 (0 of delta left) -> minus 2 gap -> 8
        checkTable(new Table(List.of(NARROW_HEADER, NARROW_HEADER), rows(ROWS_COUNT)),
            20,
            List.of(8, 8));
    }

    /**
     *
     */
    @Test
    void render_withTableShrinkingOn1Char() {
        Table table = new Table(List.of(WIDE_HEADER, NARROW_HEADER), rows(ROWS_COUNT));

        checkTable(table, table.contentWidth() - 1, List.of(WIDE_HEADER.length(), WIDEST_CONTENT.length() - 1));
    }

    /**
     *
     */
    @Test
    void render_withTableShrinkingOn2Chars() {
        Table table = new Table(List.of(WIDE_HEADER, NARROW_HEADER), rows(ROWS_COUNT));

        checkTable(table, table.contentWidth() - 2, List.of(WIDE_HEADER.length(), WIDEST_CONTENT.length() - 2));
    }

    /**
     *
     */
    @Test
    void render_withHeaderShrinking_whenRenderWidthIsLessThanTotalHeadersWidth() {
        fail("Unimplemented");
    }


    /**
     *
     */
    @Test
    void render_withHeaderShrinking_whenLastColumnWidthIsLessThanRemainingDelta() {
        fail("Unimplemented");

        // |Very ... Very Wide|Very ... Very Wide|Very ... Very Wide|Very ... Very Wide|Narrow|
    }

    /**
     *
     */
    @Test
    void render_withContentWidth() {
        int col0 = WIDE_HEADER.length();
        int col1 = WIDEST_CONTENT.length();

        Table table = new Table(List.of(WIDE_HEADER, NARROW_HEADER), rows(ROWS_COUNT));

        checkTable(table, table.contentWidth(), List.of(col0, col1));
    }

    /**
     *
     */
    @Test
    void render_nullHeaderElement() {
        fail("Unimplemented");
    }

    /**
     *
     */
    @Test
    void render_nullTableElement() {
        fail("Unimplemented");
    }

    /**
     *
     */
    @Test
    void render_emptyHeader() {
        fail("Unimplemented");
    }

    /**
     *
     */
    @Test
    void render_emtyRows() {
        fail("Unimplemented");
    }

    /**
     *
     */
    @Test
    void render_headerAndRowsCollectionSizesMismatch() {
        fail("Unimplemented");
    }

    /**
     *
     */
    @Test
    void render_rowsSizesMismatch() {
        fail("Unimplemented");
    }

    /**
     *
     */
    @Test
    void contentWidth_withContentNarrowerThanHeader() {
        String narrowHdr = NARROW_HEADER;

        List<String> hdr = List.of(narrowHdr, narrowHdr);

        List<List<?>> rows = rows(5);

        assertEquals(("Content [x,y]".length() + 2) * 2,
            new Table(hdr, rows).contentWidth());
    }

    /**
     *
     */
    @Test
    void contentWidth_withContentWiderThanHeader() {
        String wideHdr = WIDE_HEADER;

        List<String> hdr = List.of(wideHdr, wideHdr);

        List<List<?>> rows = rows(5);

        assertEquals((wideHdr.length() + 2) * 2, new Table(hdr, rows).contentWidth());
    }

    /**
     * @param rowsCnt Rows count.
     */
    private static List<List<?>> rows(int rowsCnt) {
        return IntStream.range(0, rowsCnt)
            .mapToObj(i -> List.of("Content [" + i + ",1]", "Content [" + i + ",2]"))
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

        rowsWithHdr.add(table.header());

        for (List<?> row : table.rows()) {
            rowsWithHdr.add(row.stream()
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
