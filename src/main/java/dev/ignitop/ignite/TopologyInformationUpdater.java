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
import org.apache.ignite.client.ClientCluster;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.cluster.ClusterState;

import static dev.ignitop.ignite.MetricUtils.groupServerNodesByState;
import static dev.ignitop.ignite.MetricUtils.singleMetric;
import static org.apache.ignite.cluster.ClusterState.INACTIVE;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;

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
        Set<ClusterNode> nonBaselineNodes = new HashSet<>();

        groupServerNodesByState(client, onlineBaselineNodes, offlineBaselineNodes, nonBaselineNodes);

        components.add(new Title("Topology"));

        ClientCluster cluster = client.cluster();

        // TODO: Is an oldest a coordinator?
        ClusterNode crd = cluster.forOldest().node();

        components.add(Label.normal("Ignite version:")
            .bold(crd.version())
            .spaces(2)
            .normal("Coordinator:")
            .bold("[" + crd.consistentId() + ',' + crd.hostNames() + ']')
            .build());

        ClusterState clusterState = cluster.state();

        long topVer = singleMetric(client, "io.discovery.CurrentTopologyVersion", crd.id(), Long.class, -1L);

        boolean rebalanced = singleMetric(client, "cluster.Rebalanced", crd.id(), Boolean.class, false);

        components.add(Label.normal("State:")
            .color(clusterState == INACTIVE ? RED : GREEN)
            .bold(clusterState)
            .spaces(2)
            .normal("Topology version:")
            .bold(topVer)
            .spaces(2)
            .normal("Rebalanced:")
            .color(rebalanced ? GREEN : RED)
            .bold(rebalanced)
            .build());

        components.add(new EmptySpace(2));

        addTable(components, "Online baseline nodes", nodesTable(onlineBaselineNodes));
        addTable(components, "Offline baseline nodes", offlineNodesTable(offlineBaselineNodes));
        addTable(components, "Non-baseline server nodes", nodesTable(nonBaselineNodes));
        addTable(components, "Client nodes", nodesTable(cluster.forClients().nodes()));

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
        List<String> hdr = List.of("Order", "Consistent ID", "Host names", "IP addresses", "Uptime");

        List<List<?>> rows = nodes.stream()
            .map(n -> List.of(
                n.order(),
                n.consistentId(),
                n.hostNames(),
                n.addresses(),
                formattedUptime(singleMetric(client, "sys.UpTime", n.id(), Long.class, 0L))))
            .collect(Collectors.toList());

        return new Table(hdr, rows);
    }

    /**
     * @param uptimeMillis Uptime millis.
     */
    private static String formattedUptime(long uptimeMillis) {
        long totalSeconds = uptimeMillis / 1000;

        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds / 3600)  % 24;
        long minutes = (totalSeconds / 60) % 60;
        long seconds = totalSeconds % 60;

        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
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
