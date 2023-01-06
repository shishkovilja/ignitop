package org.apache.ignite.ignitop;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientClusterGroup;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.configuration.ClientConfiguration;

public class IgniTop {

    private static final AtomicReference<Collection<ClusterNode>> servers = new AtomicReference<>(Collections.emptyList());

    public static void main(String[] args) {
        ScheduledExecutorService scheduledExecutorSrvc = Executors.newScheduledThreadPool(1);

        try (IgniteClient client = Ignition.startClient(new ClientConfiguration().setAddresses("127.0.0.1:10800"))) {
            ScheduledFuture<?> fut = scheduledExecutorSrvc.scheduleAtFixedRate(body(client), 0, 2, TimeUnit.SECONDS);

            fut.get();
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
        finally {
            scheduledExecutorSrvc.shutdown();
        }
    }

    private static Runnable body(IgniteClient client) {
        return () -> {
            ClientClusterGroup serversGrp = client.cluster().forServers();

            Collection<ClusterNode> nodes0 = servers.get();
            Collection<ClusterNode> nodes = serversGrp.nodes();

            if (nodes0.size() == nodes.size() && nodes0.containsAll(nodes))
                return;

            servers.set(nodes);

            printTable(nodesTable(nodes), "Host Names", "Consistent ID", "Node Order");
        };
    }

    private static void printTable(List<List<?>> rows, Object... columnsNames) {
        // Pre-fill max column width by header length.
        List<Integer> maxColumnWidths = Arrays.stream(columnsNames)
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

        int rowLength = columnsNames.length * 2 + maxColumnWidths.stream()
            .mapToInt(Integer::intValue)
            .sum();

        printHeader(strFormat, rowLength, columnsNames);

        for (List<?> row : rows)
            System.out.printf(strFormat, row.toArray());

        printLine(rowLength);
        System.out.println();
    }

    private static List<List<?>> nodesTable(Collection<ClusterNode> nodes) {
        List<List<?>> table = new ArrayList<>(nodes.size());

        for (ClusterNode node : nodes) {
            String hostName = String.join(",", node.hostNames());
            String consistentId = node.consistentId().toString();
            long order = node.order();

            table.add(List.of(hostName, consistentId, order));
        }

        return table;
    }

    private static void printHeader(String format, int rowLength, Object... columnNames) {
        System.out.println(">>> " + LocalDateTime.now() + ':');
        printLine(rowLength);
        System.out.printf(format, columnNames);
        printLine(rowLength);
    }

    private static void printLine(int length) {
        System.out.println(String.valueOf((char)0x02500).repeat(length));
    }
}
