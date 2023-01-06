package org.apache.ignite.ignitop;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

/**
 *
 */
public class IgniTop {
    /** Default update interval in seconds. */
    public static final int DEFAULT_UPDATE_INTERVAL = 5;

    /**
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        ScheduledExecutorService scheduledExecutorSrvc = Executors.newScheduledThreadPool(1);

        try (IgniteClient client = Ignition.startClient(new ClientConfiguration().setAddresses("127.0.0.1:10800"))) {
            ScheduledFuture<?> fut = scheduledExecutorSrvc.scheduleAtFixedRate(
                new TopologyInformationUpdater(client).getRunnable(),
                0,
                DEFAULT_UPDATE_INTERVAL,
                TimeUnit.SECONDS);

            fut.get();
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
        finally {
            scheduledExecutorSrvc.shutdown();
        }
    }

    /**
     * @param qryRes Query result.
     */
    public static void printQueryResultTable(QueryResult qryRes) {
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
            System.out.printf(strFormat, row.toArray());

        printLine(rowLength);
        System.out.println();
    }

    /**
     * @param format Format.
     * @param rowLength Row length.
     * @param columnNames Column names.
     */
    private static void printHeader(String format, int rowLength, Object... columnNames) {
        printLine(rowLength);
        System.out.printf(format, columnNames);
        printLine(rowLength);
    }

    /**
     * @param length Length.
     */
    private static void printLine(int length) {
        System.out.println(String.valueOf((char)0x02500).repeat(length));
    }
}
