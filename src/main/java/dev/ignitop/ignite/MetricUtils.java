package dev.ignitop.ignite;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.ignite.client.ClientClusterGroup;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.visor.VisorTaskArgument;
import org.apache.ignite.internal.visor.metric.VisorMetricTask;
import org.apache.ignite.internal.visor.metric.VisorMetricTaskArg;
import org.apache.ignite.internal.visor.systemview.VisorSystemViewTask;
import org.apache.ignite.internal.visor.systemview.VisorSystemViewTaskArg;
import org.apache.ignite.internal.visor.systemview.VisorSystemViewTaskResult;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class MetricUtils {
    /** Baseline node attributes system view. */
    public static final String BASELINE_NODE_ATTRIBUTES_VIEW = "BASELINE_NODE_ATTRIBUTES";

    /** Baseline nodes system view. */
    public static final String BASELINE_NODES_VIEW = "BASELINE_NODES";

    /**
     * Return result of {@link VisorSystemViewTask} execution for a node with a specified id.
     * Expected, that ID is a value, which corresponds to a value returned by a {@link ClusterNode#id()}.
     *
     * @param client      Client.
     * @param sysViewName System view name.
     * @param nodeId      Node Id.
     */
    public static List<List<?>> view(IgniteClient client, String sysViewName, UUID nodeId) {
        return view(client, sysViewName, Set.of(nodeId))
            .getOrDefault(nodeId, List.of());
    }

    /**
     * Get full result of multi-node {@link VisorSystemViewTask} execution groupped by node identifiers.
     *
     * @param client      Client.
     * @param sysViewName System view name.
     * @param nodeIds     Node ids.
     */
    public static Map<UUID, List<List<?>>> view(IgniteClient client, String sysViewName, Set<UUID> nodeIds) {
        VisorSystemViewTaskResult res = (VisorSystemViewTaskResult)executeTask(
            client,
            VisorSystemViewTask.class.getName(),
            new VisorSystemViewTaskArg(sysViewName),
            nodeIds);

        return res.rows();
    }

    /**
     * Return result of single-node {@link VisorMetricTaskArg} execution for a node with a specified id.
     * Expected, that ID is a value, which corresponds to a value returned by a {@link ClusterNode#id()}.
     *
     * @param client Client.
     * @param metricName Metric name.
     * @param nodeId Node id.
     */
    public static Map<String, ?> metric(IgniteClient client, String metricName, UUID nodeId) {
        return (Map<String, ?>)executeTask(
            client,
            VisorMetricTask.class.getName(),
            new VisorMetricTaskArg(metricName),
            Set.of(nodeId));
    }

    /**
     * Return first found entry for a specified metric name. Because {@link VisorMetricTaskArg} can return multiple
     * values, it is excpected, that this method is called for a single-value metric (not whole metric registry).
     *
     * @param client Client.
     * @param metricName Metric name.
     * @param nodeId Node id.
     * @param cls Class of a returned value.
     * @param dflt Default value.
     */
    public static <T> T singleMetric(IgniteClient client, String metricName, UUID nodeId, Class<T> cls,
        T dflt) {
        return metric(client, metricName, nodeId)
            .values()
            .stream()
            .map(obj -> (T)obj)
            .findFirst()
            .orElse(dflt);
    }

    /**
     * Retrieve all cluster nodes.
     *
     * @param client Client.
     */
    public static Set<UUID> allNodes(IgniteClient client) {
        return client.cluster()
            .nodes()
            .stream()
            .map(ClusterNode::id)
            .collect(Collectors.toSet());
    }

    /**
     * Retrieve node which client is connected to.
     *
     * @param client Client.
     */
    public static UUID connectedTo(IgniteClient client) {
        List<UUID> locNodes = client.cluster()
            .nodes()
            .stream()
            .filter(ClusterNode::isLocal)
            .map(ClusterNode::id)
            .collect(Collectors.toList());

        if (locNodes.size() == 1)
            return locNodes.get(0);
        else
            throw new IllegalStateException("Client should be connected exactly to one node, " +
                "but Cluster API returned invalid amount: " + locNodes.size());
    }

    /**
     * @param client Client.
     * @param taskCls Task class.
     * @param arg Argument.
     * @param nodeIds Node ids.
     */
    private static Object executeTask(IgniteClient client, String taskCls, Object arg, Set<UUID> nodeIds) {
        try {
            ClientClusterGroup clusterGrp = client.cluster().forNodeIds(nodeIds);

            return client.compute(clusterGrp).execute(taskCls, new VisorTaskArgument<>(nodeIds, arg, false));
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get baseline nodes attributes.
     *
     * @param client Client.
     * @param consistentIds Consistent ids.
     * @param attrs Attributes.
     */
    public static Map<String, Map<String, Object>> baselineNodesAttributes(IgniteClient client,
        Collection<?> consistentIds, String... attrs) {
        List<List<?>> allNodesAttrs = view(client, BASELINE_NODE_ATTRIBUTES_VIEW, connectedTo(client));

        List<String> attrsList = List.of(attrs);

        Map<String, Map<String, Object>> attrsMap = new HashMap<>();

        for (List<?> nodeAttr : allNodesAttrs) {
            String consistentId = String.valueOf(nodeAttr.get(0));

            if (consistentIds.contains(consistentId)) {
                String attrName = String.valueOf(nodeAttr.get(1));
                String attrVal = String.valueOf(nodeAttr.get(2));

                if (attrsList.contains(attrName))
                    attrsMap.compute(consistentId, (c, m) -> append(m, attrName, attrVal));
            }
        }

        return attrsMap;
    }


    /**
     * @param map Map.
     * @param k K.
     * @param v V.
     */
    private static <K, V> Map<K, V> append(@Nullable Map<K, V> map, K k, V v) {
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

    /**
     * Partition nodes by state: online, offline, or outside of baseline.
     *
     * @param client Client.
     * @param onlineBaselineNodes Online baseline nodes.
     * @param offlineBaselineNodes Offline baseline nodes.
     * @param nonBaselineNodes Server nodes outside of baseline.
     */
    public static void groupServerNodesByState(IgniteClient client, Collection<ClusterNode> onlineBaselineNodes,
        Collection<OfflineNodeInfo> offlineBaselineNodes, Set<ClusterNode> nonBaselineNodes) {
        List<List<?>> baselineNodesView = view(client, BASELINE_NODES_VIEW, connectedTo(client));

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

        offlineBaselineNodes.addAll(offlineByConsistentIds(client, offlineConsistentIds));

        nonBaselineNodes.addAll(nonHandledNodes);
    }

    /**
     * Get offline nodes attributes by consistent ids.
     *
     * @param client Client.
     * @param offlineConsistentIds Offline nodes consistent ids.
     */
    public static Set<OfflineNodeInfo> offlineByConsistentIds(IgniteClient client, Collection<?> offlineConsistentIds) {
        Map<String, Map<String, Object>> attrsMap = baselineNodesAttributes(client, offlineConsistentIds,
            "org.apache.ignite.ips", "TcpCommunicationSpi.comm.tcp.host.names");

        return attrsMap.entrySet()
            .stream()
            .map(e -> new OfflineNodeInfo(e.getKey(), e.getValue().get("org.apache.ignite.ips"),
                e.getValue().get("TcpCommunicationSpi.comm.tcp.host.names")))
            .collect(Collectors.toSet());
    }
}