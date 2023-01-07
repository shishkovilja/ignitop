package dev.ignitop.ignite;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import dev.ignitop.ui.component.TerminalComponent;
import dev.ignitop.ui.TerminalUI;
import dev.ignitop.ui.component.impl.Label;
import dev.ignitop.ui.component.impl.Table;
import dev.ignitop.util.QueryResult;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.cluster.ClusterNode;

import static dev.ignitop.ui.TerminalUI.EMPTY_SPACE;

/**
 *
 */
public class TopologyInformationUpdater {
    /** Previous nodes list. */
    private final AtomicReference<Collection<ClusterNode>> prevNodes = new AtomicReference<>(Collections.emptyList());

    /** Client. */
    private final IgniteClient client;
    
    /** User interface. */
    private final TerminalUI ui;

    /**
     * @param client Client.
     */
    public TopologyInformationUpdater(IgniteClient client, TerminalUI ui) {
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

        ArrayList<TerminalComponent> components = new ArrayList<>();

        components.add(new Label("Cluster information"));
        components.add(new Label("Time: " + LocalDateTime.now()));
        components.add(EMPTY_SPACE);

        components.add(new Label("Online baseline nodes:"));
        components.add(toTable(SqlQueries.onlineNodes(client)));
        components.add(EMPTY_SPACE);

        components.add(new Label("Offline baseline nodes:"));
        components.add(toTable(SqlQueries.offlineNodes(client)));
        components.add(EMPTY_SPACE);

        components.add(new Label("Other server nodes:"));
        components.add(toTable(SqlQueries.otherNodes(client)));
        components.add(EMPTY_SPACE);

        components.add(new Label("Client nodes:"));
        components.add(toTable(SqlQueries.clientNodes(client)));
        components.add(EMPTY_SPACE);

        ui.setComponents(components);
        ui.refresh();
    }

    /**
     * @param qryResult Query result.
     */
    private Table toTable(QueryResult qryResult) {
        return new Table(qryResult.columns(), qryResult.rows());
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
