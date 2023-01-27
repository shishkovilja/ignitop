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
        checkTable(80, List.of("Very very very wide header", "Narrhdr"), 25, List.of(48, 28));
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
        checkTable(20, List.of("Narrhdr", "Narrhdr"), 25, List.of(8, 8));
    }

    /**
     *
     */
    @Test
    void render_withHeaderShrinking() {
        fail("Unimplemented");
    }


    /**
     *
     */
    @Test
    void render_withContentWidth() {
        fail("Unimplemented");
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
        String narrowHdr = "Narrhdr";

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
        String wideHdr = "Very very very wide header";

        List<String> hdr = List.of(wideHdr, wideHdr);

        List<List<?>> rows = rows(5);

        assertEquals((wideHdr.length() + 2) * 2, new Table(hdr, rows).contentWidth());
    }

    /**
     * @param rowsCnt Rows count.
     */
    private List<List<?>> rows(int rowsCnt) {
        return IntStream.range(0, rowsCnt)
            .mapToObj(i -> List.of("Content [" + i + ",1]", "Content [" + i + ",2]"))
            .collect(Collectors.toList());
    }

    /**
     * @param renderWidth Render width.
     * @param hdr Header.
     * @param rowsCnt Rows count.
     * @param expColWidths Expected column widths, excluding column gap.
     */
    private void checkTable(int renderWidth, List<String> hdr, int rowsCnt, List<Integer> expColWidths) {
        List<List<?>> rows = rows(rowsCnt);

        String renderedTable = TestUtils.renderToString(new Table(hdr, rows), renderWidth);

        // Header + rows + total items line.
        assertEquals(rows.size() + 2, renderedTable.lines().count());

        ArrayList<List<String>> rowsWithHdr = new ArrayList<>();

        rowsWithHdr.add(hdr);

        for (List<?> row : rows) {
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

        String expTable = table(rowsWithHdr, expColWidths);

        assertEquals(expTable, renderedTable);
    }

    /**
     * @param rowsWithHdr Rows with a header.
     * @param colWidths Column widths.
     */
    private String table(List<List<String>> rowsWithHdr, List<Integer> colWidths) {
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
