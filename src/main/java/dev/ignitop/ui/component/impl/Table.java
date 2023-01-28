package dev.ignitop.ui.component.impl;

import java.io.PrintStream;
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

    /** Header widths sum. */
    private final int hdrWidthSum;

    /** Column widths. */
    private final List<Integer> columnWidths;

    /** Content width. */
    private int contentWidth;

    /**
     * @param hdr Header.
     * @param rows Rows.
     */
    public Table(List<String> hdr, List<List<?>> rows) {
        if (hdr.isEmpty())
            throw new IllegalArgumentException("Table columns headers list must not be empty");

        this.hdr = Collections.unmodifiableList(hdr);
        this.rows = Collections.unmodifiableList(rows);

        hdrWidths = hdr.stream()
            .map(o -> String.valueOf(o).length())
            .collect(Collectors.toUnmodifiableList());

        hdrWidthSum = hdrWidths.stream().mapToInt(i -> i).sum();

        // Pre-fill column widths by header length.
        columnWidths = new ArrayList<>(hdrWidths);

        calculateContentWidth();
    }

    /**
     *
     */
    private void calculateContentWidth() {
        for (List<?> row : rows) {
            if (row.size() != hdr.size()) {
                throw new IllegalArgumentException("Row elements count does not correspond header elements count: " +
                    "[rowSize=" + row.size() + ", hdrSize=" + hdr.size() + "]");
            }

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
    @Override public void render(int width, PrintStream out) {
        int contentWidthDelta = contentWidth - width;

        boolean dontShrinkHeaders = hdrWidthSum < width;

        int remainingDelta = contentWidthDelta;

        for (int i = 0; i < columnWidths.size(); i++) {
            int oldWidth = columnWidths.get(i);

            int columnSizeDelta = contentWidthDelta * oldWidth / contentWidth;

            if (columnSizeDelta == 0)
                columnSizeDelta = Integer.compare(contentWidthDelta, 0);

            int newWidth = oldWidth - columnSizeDelta;

            if (dontShrinkHeaders && newWidth < hdrWidths.get(i) + CELLS_GAP)
                newWidth = hdrWidths.get(i) + CELLS_GAP;

            remainingDelta -= oldWidth - newWidth;

            columnWidths.set(i, newWidth);
        }

        // Expand or shrink last element
        if (remainingDelta != 0) {
            int lastIdx = columnWidths.size() - 1;

            columnWidths.set(lastIdx, columnWidths.get(lastIdx) - remainingDelta);
        }

        String strFormat = columnWidths.stream()
            .map(l -> "%-" + l + '.' + (l - CELLS_GAP) + 's')
            .collect(Collectors.joining());

        printHeader(strFormat, out, hdr.toArray());

        for (List<?> row : rows) {
            out.printf(strFormat, row.toArray());
            out.println();
        }

        out.println("Total items: " + rows.size());
    }

    /** {@inheritDoc} */
    @Override public int contentWidth() {
        return contentWidth;
    }

    /**
     * @param format Format.
     * @param out Output stream.
     * @param columnNames Column names.
     */
    private void printHeader(String format, PrintStream out, Object... columnNames) {
        String hdrFmtStr = ansi()
            .fgBlack()
            .bg(Ansi.Color.GREEN)
            .a(format)
            .reset()
            .toString();

        out.printf(hdrFmtStr, columnNames);
        out.println();
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
}
