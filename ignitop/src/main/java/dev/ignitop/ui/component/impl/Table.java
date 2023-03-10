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
    private final List<Object[]> rows;

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
    public Table(List<String> hdr, List<Object[]> rows) {
        if (hdr.isEmpty())
            throw new IllegalArgumentException("Table columns headers list must not be empty");

        this.hdr = Collections.unmodifiableList(hdr);
        this.rows = new ArrayList<>(rows);

        hdrWidths = hdr.stream()
            .map(o -> String.valueOf(o).length())
            .collect(Collectors.toUnmodifiableList());

        hdrWidthSum = hdrWidths.stream().mapToInt(i -> i).sum();

        // Pre-fill column widths by header length.
        columnWidths = new ArrayList<>(hdrWidths);

        determineContent();
    }

    /**
     * Determine content:
     * Replace double values by a string representations with precision 1.
     * Calculate content width.
     */
    private void determineContent() {
        for (Object[] row : rows) {
            if (row.length != hdr.size()) {
                throw new IllegalArgumentException("Row elements count does not correspond header elements count: " +
                    "[rowSize=" + row.length + ", hdrSize=" + hdr.size() + "]");
            }

            for (int i = 0; i < row.length; i++) {
                Object cell = row[i];

                if (cell instanceof Double) {
                    cell = String.format("%.1f", cell);

                    row[i] = cell;
                }

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

        for (Object[] row : rows) {
            out.printf(strFormat, row);
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
    public List<Object[]> rows() {
        return Collections.unmodifiableList(rows);
    }
}
