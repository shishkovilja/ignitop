package dev.ignitop.ui.component.impl;

import java.util.ArrayList;
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

    /** A gap between cells. */
    public static final int CELLS_GAP = 2;

    /** Table header. */
    private final List<String> hdr;

    /** Table rows. */
    private final List<List<?>> rows;

    /** Header widths. */
    private final List<Integer> hdrWidths;

    /** Column widths. */
    private final List<Integer> columnWidths;

    /** Content width. */
    private int contentWidth;

    /**
     * @param hdr Header.
     * @param rows Rows.
     */
    public Table(List<String> hdr, List<List<?>> rows) {
        this.hdr = Collections.unmodifiableList(hdr);
        this.rows = Collections.unmodifiableList(rows);

        hdrWidths = hdr.stream()
            .map(o -> String.valueOf(o).length() + CELLS_GAP)
            .collect(Collectors.toUnmodifiableList());

        // Pre-fill column widths by header length.
        columnWidths = new ArrayList<>(hdrWidths);

        calculateContentWidth();
    }

    /**
     *
     */
    private void calculateContentWidth() {
        for (List<?> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                Object cell = row.get(i);

                int elementSize = String.valueOf(cell).length();

                if (elementSize > columnWidths.get(i))
                    columnWidths.set(i, elementSize);
            }
        }

        columnWidths.replaceAll(l -> l + CELLS_GAP);

        contentWidth = columnWidths.stream()
            .mapToInt(Integer::intValue)
            .sum();
    }

    /** {@inheritDoc} */
    @Override public void render(Terminal terminal, int width) {
        int widthDelta = contentWidth - width;

        for (int i = 0; i < columnWidths.size(); i++) {
            int columnSizeDelta = widthDelta / (columnWidths.size() - i);

            if (columnSizeDelta == 0)
                columnSizeDelta = widthDelta < 0 ? -1 : 1;

            int oldWidth = columnWidths.get(i);

            int newWidth = oldWidth - columnSizeDelta;

            if (newWidth < hdrWidths.get(i))
                newWidth = hdrWidths.get(i);

            widthDelta -= oldWidth - newWidth;

            columnWidths.set(i, newWidth);
        }

        // Expand or shrink last element
        if (widthDelta != 0) {
            int lastIdx = columnWidths.size() - 1;

            columnWidths.set(lastIdx, columnWidths.get(lastIdx) - widthDelta);
        }

        String strFormat = columnWidths.stream()
            .map(l -> "%-" + l + '.' + (l - CELLS_GAP) + 's')
            .collect(Collectors.joining()) + "%n";

        printHeader(terminal, strFormat, width, hdr.toArray());

        for (List<?> row : rows)
            terminal.out().printf(strFormat, row.toArray());

        fillLine(terminal, width, HORIZONTAL_LINE);
    }

    /** {@inheritDoc} */
    @Override public int contentWidth() {
        return contentWidth;
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
