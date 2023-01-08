package dev.ignitop.ignite;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import dev.ignitop.ui.component.TerminalComponent;
import dev.ignitop.ui.TerminalUI;
import dev.ignitop.ui.component.impl.Header;
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
    private final AtomicReference<Collection<ClusterNode>> prevNodesRef = new AtomicReference<>(Collections.emptyList());

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
    //TODO: It is not correct place for body. UI resize and other should be handled in some unified runnable.
    public void body() {
        if (ui.resized() || hasTopologyChanges()) {
            QueryResult crdRes = SqlQueries.coordinator(client);
            QueryResult topVerRes = SqlQueries.topologyVersion(client);
            QueryResult clusterSummaryRes = SqlQueries.clusterSummary(client);

            ArrayList<TerminalComponent> components = new ArrayList<>();

            components.add(new Header("Cluster information"));

            components.add(Label.bold("Time:")
                .normal(LocalDateTime.now())
                .build());

            components.add(EMPTY_SPACE);

            components.add(Label.underline("Online baseline nodes:").build());
            components.add(toTable(SqlQueries.onlineNodes(client)));

            components.add(EMPTY_SPACE);

            components.add(Label.underline("Offline baseline nodes:").build());
            components.add(toTable(SqlQueries.offlineNodes(client)));
            components.add(EMPTY_SPACE);

            components.add(Label.underline("Other server nodes:").build());
            components.add(toTable(SqlQueries.otherNodes(client)));
            components.add(EMPTY_SPACE);

            components.add(Label.underline("Client nodes:").build());
            components.add(toTable(SqlQueries.clientNodes(client)));
            components.add(EMPTY_SPACE);

            ui.setComponents(components);
            ui.refresh();
        }
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
    private boolean hasTopologyChanges() {
        Collection<ClusterNode> prevNodes = prevNodesRef.get();
        Collection<ClusterNode> curNodes = client.cluster().nodes();

        prevNodesRef.set(curNodes);

        return prevNodes.size() != curNodes.size() || !prevNodes.containsAll(curNodes);
    }
}
