package dev.ignitop.ignite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import dev.ignitop.ui.TerminalUI;
import dev.ignitop.ui.component.TerminalComponent;
import dev.ignitop.ui.component.impl.EmptySpace;
import dev.ignitop.ui.component.impl.Header;
import dev.ignitop.ui.component.impl.Label;
import dev.ignitop.ui.component.impl.Table;
import dev.ignitop.ui.component.impl.Title;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.cluster.ClusterNode;

import static dev.ignitop.ignite.MetricUtils.groupServerNodesByState;
import static dev.ignitop.ignite.MetricUtils.metricValue;

/**
 *
 */
public class TopologyInformationUpdater {
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
    //TODO: It is not correct place for body, see: https://github.com/shishkovilja/ignitop/issues/25
    public void body() {
        ArrayList<TerminalComponent> components = new ArrayList<>();

        Set<ClusterNode> onlineBaselineNodes = new HashSet<>();
        Set<OfflineNodeInfo> offlineBaselineNodes = new HashSet<>();
        Set<ClusterNode> otherServerNodes = new HashSet<>();

        groupServerNodesByState(client, onlineBaselineNodes, offlineBaselineNodes, otherServerNodes);

        components.add(new Title("Topology"));

        ClusterNode crd = client.cluster().forOldest().node();

        components.add(Label.normal("Ignite version:")
            .bold(crd.version())
            .spaces(2)
            .normal("Coordinator:")
            .bold("[" + crd.consistentId() + ',' + crd.hostNames() + ']')
            .build());

        Object clusterState = metricValue(client, "ignite.clusterState");
        Object topVer = metricValue(client, "io.discovery.CurrentTopologyVersion");
        Object rebalanced = metricValue(client, "cluster.Rebalanced");

        components.add(Label.normal("State:")
            .bold(clusterState)
            .spaces(2)
            .normal("Topology version:")
            .bold(topVer)
            .spaces(2)
            .normal("Rebalanced:")
            .bold(rebalanced)
            .build());

        components.add(new EmptySpace(2));

        addTable(components, "Online baseline nodes", nodesTable(onlineBaselineNodes));
        addTable(components, "Offline baseline nodes", offlineNodesTable(offlineBaselineNodes));
        addTable(components, "Other server nodes", nodesTable(otherServerNodes));
        addTable(components, "Client nodes", nodesTable(client.cluster().forClients().nodes()));

        ui.setComponents(components);
        ui.refresh();
    }

    /**
     * Add table with header and surrounding empty spaces.
     *
     * @param components Components.
     * @param hdr Table header.
     * @param tbl Table.
     */
    private void addTable(List<TerminalComponent> components, String hdr, Table tbl) {
        components.add(new Header(hdr));
        components.add(tbl);
        components.add(new EmptySpace(2));
    }

    /**
     * @param nodes Nodes.
     */
    private Table nodesTable(Collection<ClusterNode> nodes) {
        List<String> hdr = List.of("Order", "Consistent ID", "Host names", "IP addresses");

        List<List<?>> rows = nodes.stream()
            .map(n -> List.of(n.order(), n.consistentId(), n.hostNames(), n.addresses()))
            .collect(Collectors.toList());

        return new Table(hdr, rows);
    }

    /**
     * @param nodes Nodes.
     */
    private Table offlineNodesTable(Collection<OfflineNodeInfo> nodes) {
        List<String> hdr = List.of("Consistent ID", "Host names", "IP addresses");

        List<List<?>> rows = nodes.stream()
            .map(n -> List.of(n.consistentId(), n.hostNames(), n.addresses()))
            .collect(Collectors.toList());

        return new Table(hdr, rows);
    }
}
