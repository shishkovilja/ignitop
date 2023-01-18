package dev.ignitop.ignite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import dev.ignitop.ui.TerminalUI;
import dev.ignitop.ui.component.TerminalComponent;
import dev.ignitop.ui.component.impl.EmptySpace;
import dev.ignitop.ui.component.impl.Header;
import dev.ignitop.ui.component.impl.Label;
import dev.ignitop.ui.component.impl.Table;
import dev.ignitop.ui.component.impl.Title;
import dev.ignitop.util.QueryResult;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.cluster.ClusterNode;
import org.jetbrains.annotations.Nullable;

import static dev.ignitop.ignite.MetricUtils.metricValue;
import static dev.ignitop.ignite.MetricUtils.viewOfRandomNode;

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

        mapServerNodesByType(onlineBaselineNodes, offlineBaselineNodes, otherServerNodes);

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

    // TODO: refactor boilerplate
    // TODO: fix incorrect info
    private Set<OfflineNodeInfo> offlineByConsistentIds(Collection<?> offlineBaselineNodesId) {
        List<List<?>> allNodesAttrs = viewOfRandomNode(client, "BASELINE_NODE_ATTRIBUTES");

        List<String> attrsFilter = List.of("org.apache.ignite.ips", "TcpCommunicationSpi.comm.tcp.host.names");

        Map<String, Map<String, Object>> attrsMap = new HashMap<>();

        for (List<?> nodeAttr : allNodesAttrs) {
            String consistentId = String.valueOf(nodeAttr.get(0));

            if (offlineBaselineNodesId.contains(consistentId)) {
                String attrName = String.valueOf(nodeAttr.get(1));
                String attrVal = String.valueOf(nodeAttr.get(2));

                if (attrsFilter.contains(attrName))
                    attrsMap.compute(consistentId, (c, m) -> append(m, attrName, attrVal));
            }
        }

        return attrsMap.entrySet()
            .stream()
            .map(e -> new OfflineNodeInfo(e.getKey(), e.getValue().get("org.apache.ignite.ips"),
                e.getValue().get("TcpCommunicationSpi.comm.tcp.host.names")))
            .collect(Collectors.toSet());
    }

    private <K, V> Map<K, V> append(@Nullable Map<K, V> map, K k, V v) {
        if (map == null) {
            Map<K, V> map0 = new HashMap<>();
            map0.put(k, v);

            return map0;
        }
        else {
            map.put(k, v);

            return map;
        }
    }

    private void mapServerNodesByType(Collection<ClusterNode> onlineBaselineNodes,
        Collection<OfflineNodeInfo> offlineBaselineNodes,
        Set<ClusterNode> otherServerNodes) {
        List<List<?>> baselineNodesView = viewOfRandomNode(client, "BASELINE_NODES");

        Set<ClusterNode> nonHandledNodes = new HashSet<>(client.cluster().forServers().nodes());

        Set<Object> offlineConsistentIds = new HashSet<>();

        for (List<?> nodeInfo : baselineNodesView) {
            String consistentId = String.valueOf(nodeInfo.get(0));
            boolean online = (boolean)nodeInfo.get(1);

            Optional<ClusterNode> nodeOpt = nonHandledNodes.stream()
                .filter(n -> consistentId.equals(String.valueOf(n.consistentId())))
                .findFirst();

            if (nodeOpt.isPresent() && online) {
                onlineBaselineNodes.add(nodeOpt.get());

                nonHandledNodes.remove(nodeOpt.get());
            }
            else
                offlineConsistentIds.add(consistentId);
        }

        offlineBaselineNodes.addAll(offlineByConsistentIds(offlineConsistentIds));

        otherServerNodes.addAll(nonHandledNodes);
    }

    /**
     * @param qryResult Query result.
     */
    private static Object value(QueryResult qryResult) {
        return qryResult.rows().get(0).get(0);
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
     * @param qryResult Query result.
     */
    private Table nodesTable(QueryResult qryResult) {
        return new Table(qryResult.columns(), qryResult.rows());
    }

    private Table nodesTable(Collection<ClusterNode> nodes) {
        List<String> hdr = List.of("Order", "Consistent ID", "Host names", "IP addresses");

        List<List<?>> rows = nodes.stream()
            .map(n -> List.of(n.order(), n.consistentId(), n.hostNames(), n.addresses()))
            .collect(Collectors.toList());

        return new Table(hdr, rows);
    }

    private Table offlineNodesTable(Collection<OfflineNodeInfo> nodes) {
        List<String> hdr = List.of("Consistent ID", "Host names", "IP addresses");

        List<List<?>> rows = nodes.stream()
            .map(n -> List.of(n.consistentId(), n.hostNames(), n.addresses()))
            .collect(Collectors.toList());

        return new Table(hdr, rows);
    }
}
