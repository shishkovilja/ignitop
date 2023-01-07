package dev.ignitop.ui.component.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import dev.ignitop.ui.Terminal;
import dev.ignitop.ui.component.TerminalComponent;

/**
 *
 */
public class Table implements TerminalComponent {
    /** Horizontal line character. */
    public static final char HORIZONTAL_LINE = (char)0x02500;

    /** Table header. */
    private final List<String> hdr;

    /** Table rows. */
    private final List<List<?>> rows;

    /**
     * @param hdr Header.
     * @param rows Rows.
     */
    public Table(List<String> hdr, List<List<?>> rows) {
        this.hdr = Collections.unmodifiableList(hdr);
        this.rows = Collections.unmodifiableList(rows);
    }

    /**
     * @return Table header.
     */
    public List<String> header() {
        return hdr;
    }

    /**
     * @return Table rows.
     */
    public List<List<?>> rows() {
        return rows;
    }

    /** {@inheritDoc} */
    @Override public void renderWith(Terminal terminal) {
        // Pre-fill max column width by header length.
        List<Integer> maxColumnWidths = hdr.stream()
            .map(o -> String.valueOf(o).length())
            .collect(Collectors.toList());

        for (List<?> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                Object cell = row.get(i);

                int elementSize = String.valueOf(cell).length();

                if (elementSize > maxColumnWidths.get(i))
                    maxColumnWidths.set(i, elementSize);
            }
        }

        String strFormat = maxColumnWidths.stream()
            .map(l -> "%-" + (l + 2) + '.' + l + 's')
            .collect(Collectors.joining()) + "%n";

        int rowLength = hdr.size() * 2 + maxColumnWidths.stream()
            .mapToInt(Integer::intValue)
            .sum();

        printHeader(terminal, strFormat, rowLength, hdr.toArray());

        for (List<?> row : rows)
            terminal.out().printf(strFormat, row.toArray());

        fillLine(terminal, rowLength, HORIZONTAL_LINE);
    }

    /**
     * @param terminal Terminal.
     * @param length Length.
     * @param c Character for liine filling.
     */
    private void fillLine(Terminal terminal, int length, char c) {
        terminal.out().println(String.valueOf(c).repeat(length));
    }

    /**
     * @param terminal Terminal.
     * @param format Format.
     * @param rowLength Row length.
     * @param columnNames Column names.
     */
    private void printHeader(Terminal terminal, String format, int rowLength, Object... columnNames) {
        fillLine(terminal, rowLength, HORIZONTAL_LINE);
        terminal.out().printf(format, columnNames);
        fillLine(terminal, rowLength, HORIZONTAL_LINE);
    }
}
