package dev.ignitop.ui.component.impl;

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
        String veryWideHdr = "Very very very wide column";
        String narrowHdr = "Narrcol";

        List<String> hdr = List.of(veryWideHdr, narrowHdr);

        List<List<?>> rows = rows(5);

        String renderedTable = TestUtils.renderToString(new Table(hdr, rows), 80);

        // Header + rows + total items line
        assertEquals(rows.size() + 2, renderedTable.lines().count());

        int cellsGap = 2;

        // Width is derived from the widest element, i.e. column header name itself
        int expWideColWidth = veryWideHdr.length() + cellsGap + 24;

        // Width is derived from the widest element, i.e. from column content
        int expNarrowColWidth = "Content [x,y]".length() + cellsGap + 13;

        String expTable = table(List.of(veryWideHdr, narrowHdr), rows, List.of(expWideColWidth, expNarrowColWidth));

        assertEquals(expTable, renderedTable);
    }

    /**
     *
     */
    @Test
    void render_withShrinking() {
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
        String wideHdr = "Very very very wide column";

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
     * @param hdr Header.
     * @param rows Rows.
     * @param colWidths Column widths.
     */
    private String table(List<String> hdr, List<List<?>> rows, List<Integer> colWidths) {
        String rowFormat = String.format("%%-%ds".repeat(colWidths.size()), colWidths.toArray());

        String expHdr = ansi()
            .fgBlack()
            .bgGreen()
            .a(String.format(rowFormat, hdr.toArray()))
            .reset().toString();

        String expRows = rows.stream()
            .map(l -> String.format(rowFormat, l.toArray()))
            .collect(Collectors.joining(lineSeparator()));

        return String.join(lineSeparator(), expHdr, expRows,"Total items: " + rows.size()) +
            lineSeparator();
    }
}
