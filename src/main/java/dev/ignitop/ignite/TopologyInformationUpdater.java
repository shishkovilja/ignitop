package dev.ignitop.ignite;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import dev.ignitop.ui.UserInterface;
import dev.ignitop.util.QueryResult;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.cluster.ClusterNode;

/**
 *
 */
public class TopologyInformationUpdater {
    /** Previous nodes list. */
    private final AtomicReference<Collection<ClusterNode>> prevNodes = new AtomicReference<>(Collections.emptyList());

    /** Client. */
    private final IgniteClient client;
    
    /** User interface. */
    private final UserInterface ui;

    /**
     * @param client Client.
     */
    public TopologyInformationUpdater(IgniteClient client, UserInterface ui) {
        this.client = client;
        this.ui = ui;
    }

    /**
     *
     */
    public void body() {
        if (noTopologyChanges())
            return;

        QueryResult crdRes = SqlQueries.coordinator(client);
        QueryResult topVerRes = SqlQueries.topologyVersion(client);
        QueryResult clusterSummaryRes = SqlQueries.clusterSummary(client);

        ui.clearScreen();

        ui.label(">>>>>> Topology Information: " + LocalDateTime.now() + " <<<<<<");

        ui.printQueryResultWithLabel(SqlQueries.onlineNodes(client), ">>>>>> Online baseline nodes:");
        ui.printQueryResultWithLabel(SqlQueries.offlineNodes(client), ">>>>>> Offline baseline nodes:");
        ui.printQueryResultWithLabel(SqlQueries.otherNodes(client), ">>>>>> Other server nodes:");
        ui.printQueryResultWithLabel(SqlQueries.clientNodes(client), ">>>>>> Client nodes:");

        ui.label(">>>>>> End of topology Information <<<<<<");
        ui.emptyLine();
    }

    /**
     *
     */
    private boolean noTopologyChanges() {
        Collection<ClusterNode> nodes0 = prevNodes.get();
        Collection<ClusterNode> nodes = client.cluster().nodes();

        prevNodes.set(nodes);

        return nodes0.size() == nodes.size() && nodes0.containsAll(nodes);
    }
}
