package org.apache.ignite.ignitop;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.cluster.ClusterNode;

import static org.apache.ignite.ignitop.IgniTop.printQueryResultTable;

/**
 *
 */
public class TopologyInformationUpdater {
    /** Servers. */
    private final AtomicReference<Collection<ClusterNode>> nodesHolder = new AtomicReference<>(Collections.emptyList());

    /** Client. */
    private final IgniteClient client;

    /**
     * @param client Client.
     */
    public TopologyInformationUpdater(IgniteClient client) {
        this.client = client;
    }

    /**
     *
     */
    public Runnable getRunnable() {
        return () -> {
            if (noTopologyChanges())
                return;

            QueryResult crdRes = SqlQueries.coordinator(client);
            QueryResult topVerRes = SqlQueries.topologyVersion(client);
            QueryResult clusterSummaryRes = SqlQueries.clusterSummary(client);

            IgniTop.clearScreen();

            System.out.println(">>>>>> Topology Information: " + LocalDateTime.now() + " <<<<<<");

            printWithLabel(SqlQueries.onlineNodes(client), ">>>>>> Online baseline nodes:");
            printWithLabel(SqlQueries.offlineNodes(client), ">>>>>> Offline baseline nodes:");
            printWithLabel(SqlQueries.otherNodes(client), ">>>>>> Other server nodes:");
            printWithLabel(SqlQueries.clientNodes(client), ">>>>>> Client nodes:");

            System.out.println(">>>>>> End of topology Information <<<<<<");
            System.out.println();
        };
    }

    /**
     * @param qryRes Query result.
     * @param lbl Label for table.
     */
    private void printWithLabel(QueryResult qryRes, String lbl) {
        if (qryRes.isEmpty())
            return;

        System.out.println(lbl);
        printQueryResultTable(qryRes);
    }

    /**
     *
     */
    private boolean noTopologyChanges() {
        Collection<ClusterNode> nodes0 = nodesHolder.get();
        Collection<ClusterNode> nodes = client.cluster().nodes();

        nodesHolder.set(nodes);

        return nodes0.size() == nodes.size() && nodes0.containsAll(nodes);
    }
}
