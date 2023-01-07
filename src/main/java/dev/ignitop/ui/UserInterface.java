package dev.ignitop.ui;

import dev.ignitop.util.QueryResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class UserInterface {
    /** Horizontal line character. */
    public static final char HORIZONTAL_LINE = (char)0x02500;

    /** Terminal. */
    private final Terminal terminal;

    /**
     * @param terminal Terminal.
     */
    public UserInterface(Terminal terminal) {
        this.terminal = terminal;
    }

    /**
     * @param text Label.
     */
    public void label(String text) {
        terminal.out().println(text);
    }

    /**
     * @param qryRes Query result.
     * @param lbl Label for table.
     */
    public void printQueryResultWithLabel(QueryResult qryRes, String lbl) {
        if (qryRes.isEmpty())
            return;

        label(lbl);
        printQueryResultTable(qryRes);
    }

    /**
     * @param qryRes Query result.
     */
    public void printQueryResultTable(QueryResult qryRes) {
        // Pre-fill max column width by header length.
        List<Integer> maxColumnWidths = qryRes.columns()
                .stream()
                .map(o -> String.valueOf(o).length())
                .collect(Collectors.toList());

        for (List<?> row : qryRes.rows()) {
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

        int rowLength = qryRes.columns().size() * 2 + maxColumnWidths.stream()
                .mapToInt(Integer::intValue)
                .sum();

        printHeader(strFormat, rowLength, qryRes.columns().toArray());

        for (List<?> row : qryRes.rows())
            terminal.out().printf(strFormat, row.toArray());

        line(rowLength, HORIZONTAL_LINE);
        terminal.out().println();
    }

    /**
     * @param format Format.
     * @param rowLength Row length.
     * @param columnNames Column names.
     */
    private void printHeader(String format, int rowLength, Object... columnNames) {
        line(rowLength, HORIZONTAL_LINE);
        terminal.out().printf(format, columnNames);
        line(rowLength, HORIZONTAL_LINE);
    }

    /**
     * @param length Length of a line.
     * @param c Character from which a line is filled.
     */
    private void line(int length, char c) {
        label(String.valueOf(c).repeat(length));
    }

    /**
     *
     */
    public void clearScreen() {
        terminal.out().print("\033[H\033[2J");
        terminal.out().flush();
    }

    /**
     *
     */
    public void emptyLine() {
        terminal.out().println();
    }
}
