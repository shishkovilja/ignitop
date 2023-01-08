package dev.ignitop.ui.component.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import dev.ignitop.ui.component.TerminalComponent;
import org.fusesource.jansi.Ansi;

import static org.fusesource.jansi.Ansi.ansi;

/**
 *
 */
public class Table implements TerminalComponent {
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
    @Override public void render(int width) {
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
            .collect(Collectors.joining());

        printHeader(strFormat, hdr.toArray());

        for (List<?> row : rows) {
            System.out.printf(strFormat, row.toArray());
            System.out.println();
        }

        System.out.println("Total items: " + rows.size());
    }

    /** {@inheritDoc} */
    @Override public int contentWidth() {
        return contentWidth;
    }

    /**
     * @param format Format.
     * @param columnNames Column names.
     */
    private void printHeader(String format, Object... columnNames) {
        String hdrFmtStr = ansi()
            .fgBlack()
            .bg(Ansi.Color.WHITE)
            .a(format)
            .reset()
            .toString();

        System.out.printf(hdrFmtStr, columnNames);
        System.out.println();
    }
}
