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
        String veryWideColumn = "Very very very wide column";
        String narrowColumn = "Narrcol";

        List<String> hdr = List.of(veryWideColumn, narrowColumn);

        List<List<?>> rows = IntStream.range(0, 5)
            .mapToObj(i -> List.of("Content [" + i + ",1]", "Content [" + i + ",2]"))
            .collect(Collectors.toList());

        String renderedTable = TestUtils.renderToString(new Table(hdr, rows), 80);

        // Table + line separator
        assertEquals(7, renderedTable.lines().count());

        int cellsGap = 2;

        // Width is derived from the widest element, i.e. column header name itself
        int expVeryWideColumnWidth = veryWideColumn.length() + cellsGap + 24;

        // Width is derived from the widest element, i.e. from column content
        int expNarrowColumnWidth = "Content [x,y]".length() + cellsGap + 13;

        String rowFormat = String.format("%%-%ds%%-%ds", expVeryWideColumnWidth, expNarrowColumnWidth);

        String expHdr = ansi()
            .fgBlack()
            .bgGreen()
            .a(String.format(rowFormat, veryWideColumn, narrowColumn))
            .reset().toString();

        String expRows = rows.stream()
            .map(l -> String.format(rowFormat, l.get(0), l.get(1)))
            .collect(Collectors.joining(lineSeparator()));

        String expTable = String.join(lineSeparator(), expHdr, expRows,"Total items: " + rows.size()) +
            lineSeparator();

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
        String narrowColumn = "Narrcol";

        List<String> hdr = List.of(narrowColumn, narrowColumn);

        List<List<?>> rows = IntStream.range(0, 5)
            .mapToObj(i -> List.of("Content [" + i + ",1]", "Content [" + i + ",2]"))
            .collect(Collectors.toList());

        assertEquals(("Content [x,y]".length() + 2) * 2,
            new Table(hdr, rows).contentWidth());
    }

    /**
     *
     */
    @Test
    void contentWidth_withContentWiderThanHeader() {
        String veryWideColumn = "Very very very wide column";

        List<String> hdr = List.of(veryWideColumn, veryWideColumn);

        List<List<?>> rows = IntStream.range(0, 5)
            .mapToObj(i -> List.of("Content [" + i + ",1]", "Content [" + i + ",2]"))
            .collect(Collectors.toList());

        assertEquals((veryWideColumn.length() + 2) * 2, new Table(hdr, rows).contentWidth());
    }
}
