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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UnknownFormatConversionException;
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

    /** Ascending sorting character. */
    public static final char ASC_CHAR = '△';

    /** Descending sorting character. */
    public static final char DESC_CHAR = '▽';

    /** Table header. */
    private final List<String> hdr;

    /** Table rows "as is". */
    private final List<Object[]> rawRows;

    /** Header widths. */
    private final List<Integer> hdrWidths;

    /** Header widths sum. */
    private final int hdrWidthSum;

    /** Column widths. */
    private final List<Integer> columnWidths;

    /** Content width. */
    private int contentWidth;

    /** Last rendered width. */
    private int lastRenderedWidth;

    /** Sorting column index. */
    private int sortingColIdx;

    /** Ascending sorting flag. */
    private boolean ascSorting;

    /**
     * @param hdr Header.
     * @param rawRows Rows.
     */
    public Table(List<String> hdr, List<Object[]> rawRows) {
        if (hdr.isEmpty())
            throw new IllegalArgumentException("Table columns headers list must not be empty");

        this.hdr = Collections.unmodifiableList(hdr);
        this.rawRows = new ArrayList<>(rawRows);

        hdrWidths = hdr.stream()
            .map(o -> String.valueOf(o).length())
            .collect(Collectors.toUnmodifiableList());

        hdrWidthSum = hdrWidths.stream().mapToInt(i -> i).sum();

        // Pre-fill column widths by header length.
        columnWidths = new ArrayList<>(hdrWidths);

        sortingColIdx = 0;
        ascSorting = true;

        determineContent();
    }

    /**
     * Determine content:
     * Replace double values by a string representations with precision 1.
     * Calculate content width.
     */
    private void determineContent() {
        for (Object[] row : rawRows) {
            if (row.length != hdr.size()) {
                throw new IllegalArgumentException("Row elements count does not correspond header elements count: " +
                    "[rowSize=" + row.length + ", hdrSize=" + hdr.size() + "]");
            }

            for (int i = 0; i < row.length; i++) {
                String cell = str(row[i]);

                int cellWidth = cell.length();

                if (cellWidth > columnWidths.get(i))
                    columnWidths.set(i, cellWidth);
            }
        }

        columnWidths.replaceAll(l -> l + CELLS_GAP);

        contentWidth = columnWidths.stream()
            .mapToInt(Integer::intValue)
            .sum();

        // Necessary to initial rendering
        lastRenderedWidth = contentWidth;
    }

    /** {@inheritDoc} */
    @Override public void render(int width, PrintStream out) {
        int contentWidthDelta = lastRenderedWidth - width;

        boolean dontShrinkHeaders = hdrWidthSum <= width;

        int remainingDelta = contentWidthDelta;

        for (int i = 0; i < columnWidths.size(); i++) {
            int oldWidth = columnWidths.get(i);

            int columnSizeDelta = contentWidthDelta * oldWidth / contentWidth;

            if (columnSizeDelta == 0)
                columnSizeDelta = Integer.compare(contentWidthDelta, 0);

            int newWidth = oldWidth - columnSizeDelta;

            if (newWidth < 0)
                newWidth = 0;

            if (dontShrinkHeaders && newWidth < hdrWidths.get(i) + CELLS_GAP)
                newWidth = hdrWidths.get(i) + CELLS_GAP;

            remainingDelta -= oldWidth - newWidth;

            columnWidths.set(i, newWidth);
        }

        // Expand or shrink last element
        if (remainingDelta != 0) {
            int lastIdx = columnWidths.size() - 1;

            int newWidth = columnWidths.get(lastIdx) - remainingDelta;

            if (newWidth < 0)
                newWidth = 0;

            columnWidths.set(lastIdx, newWidth);
        }

        printHeader(out);

        String fmt = columnWidths.stream()
            .map(l -> format(l, CELLS_GAP))
            .collect(Collectors.joining());

        for (Object[] row : rows()) {
            out.printf(fmt, stringify(row));
            out.println();
        }

        out.println("Total items: " + rawRows.size());

        lastRenderedWidth = width;
    }

    /** {@inheritDoc} */
    @Override public int contentWidth() {
        return contentWidth;
    }

    /**
     * @return Table header.
     */
    public List<String> header() {
        return hdr;
    }

    /**
     * Return sorted rows of a table. Comparing logic is perfromed by {@link #compareRows(Object[], Object[])} method.
     *
     * @return Table rows.
     */
    public Collection<Object[]> rows() {
        return rawRows.stream()
            .sorted(this::compareRows)
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * @param colIdx Index of column which will be user to sort rows.
     * @param ascending Ascending sorting. Set <code>false</code> for descending sorting.
     */
    public void setSorting(int colIdx, boolean ascending) {
        if (colIdx < 0 || colIdx >= hdr.size())
            return;

        sortingColIdx = colIdx;
        ascSorting = ascending;
    }

    /**
     * @return Sorting column index.
     */
    public int sortingColumnIndex() {
        return sortingColIdx;
    }

    /**
     * @return Ascending sorting flag.
     */
    public boolean ascendingSorting() {
        return ascSorting;
    }

    /**
     * @param out Output stream.
     */
    private void printHeader(PrintStream out) {
        List<String> formattedHdr = new ArrayList<>(hdr.size());

        for (int i = 0; i < hdr.size(); i++) {
            int colWidth = columnWidths.get(i);
            String hdrCell = hdr.get(i);

            // Manually shrink of header cell of sorted column, because we need to append sorting character.
            if (i == sortingColIdx) {
                int width = colWidth - CELLS_GAP;

                if (width < 0)
                    width = 0;

                hdrCell = hdrCell.length() > width ? hdrCell.substring(0, width) : hdrCell;

                hdrCell += ascSorting ? ASC_CHAR : DESC_CHAR;

                formattedHdr.add(String.format(format(colWidth, CELLS_GAP - 1), hdrCell));
            }
            else {
                String fmt = format(colWidth, CELLS_GAP);

                try {
                    formattedHdr.add(String.format(fmt, hdrCell));
                }
                catch (UnknownFormatConversionException e) {
                    throw new RuntimeException("Incorrect format: [colWidth=" + colWidth + ", hdrCell="
                        + hdrCell + ", fmt=" + fmt + ']', e);
                }
            }
        }

        String hdrBefore = coloredHeader(Ansi.Color.GREEN, formattedHdr.subList(0, sortingColIdx));

        String sortingCol = coloredHeader(Ansi.Color.BLUE, List.of(formattedHdr.get(sortingColIdx)));

        String hdrAfter = coloredHeader(Ansi.Color.GREEN, formattedHdr.subList(sortingColIdx + 1, hdr.size()));

        out.println(hdrBefore + sortingCol + hdrAfter);
    }

    /**
     * @param colWidth Column width.
     */
    private String format(int colWidth, int cellsGap) {
        int width = colWidth - cellsGap;

        if (width < 0) {
            width = 0;

            colWidth = cellsGap;
        }

        return "%-" + colWidth + '.' + width + 's';
    }

    /**
     * @param color Color.
     * @param cells Header cells.
     */
    private String coloredHeader(Ansi.Color color, List<String> cells) {
        if (cells.isEmpty())
            return "";

        return ansi()
            .fgBlack()
            .bg(color)
            .a(String.join("", cells))
            .reset()
            .toString();
    }

    /**
     * "Stringify" row elements.
     *
     * @param row Row.
     */
    private Object[] stringify(Object[] row) {
        Object[] strs = new Object[row.length];

        for (int i = 0; i < row.length; i++)
            strs[i] = str(row[i]);

        return strs;
    }

    /**
     * Gets string representation of an object.
     *
     * @param obj Object.
     */
    private String str(Object obj) {
        return obj instanceof Double ? String.format("%.1f", (Double)obj) : String.valueOf(obj);
    }

    /**
     * Compare rows according sorting parameters: by column and ascending or descending order.
     * If column values are instances of {@link Comparable}, then {@link Comparable#compareTo(Object)} will be used
     * to compare elements. Otherwise, string representations of an objects will be compared.
     *
     * @param row1 Row 1.
     * @param row2 Row 2.
     */
    private int compareRows(Object[] row1, Object[] row2) {
        Object obj1 = row1[sortingColIdx];
        Object obj2 = row2[sortingColIdx];

        Comparator<Object> comp;

        if (obj1 instanceof Comparable && obj2 instanceof Comparable)
            comp = (o1, o2) -> ((Comparable<Object>)o1).compareTo(o2);
        else
            comp = Comparator.comparing(String::valueOf);

        comp = ascSorting ? comp : comp.reversed();

        return comp.compare(obj1, obj2);
    }
}
