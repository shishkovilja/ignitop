package dev.ignitop.ignite;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
    /** Baseline node attributes system view name. */
    public static final String BASELINE_NODE_ATTRIBUTES = "BASELINE_NODE_ATTRIBUTES";

    /**
     * @param client Client.
     * @param sysViewName System view name.
     */
    public static VisorSystemViewTaskResult view(IgniteClient client, String sysViewName) {
        return view(client, sysViewName, Collections.singleton(connectedTo(client)));
    }

    /**
     * @param client Client.
     * @param sysViewName System view name.
     * @param nodeIds Node ids.
     */
    public static VisorSystemViewTaskResult view(IgniteClient client, String sysViewName, Set<UUID> nodeIds) {
        return (VisorSystemViewTaskResult)executeTask(
            client,
            VisorSystemViewTask.class.getName(),
            new VisorSystemViewTaskArg(sysViewName),
            nodeIds);
    }

    /**
     * @param client Client.
     * @param sysViewName System view name.
     */
    public static List<List<?>> viewOfRandomNode(IgniteClient client, String sysViewName) {
        return view(client, sysViewName)
            .rows()
            .values()
            .stream()
            .findFirst()
            .orElse(Collections.emptyList());
    }

    /**
     * @param client Client.
     * @param metricName Metric name.
     */
    public static Map<String, ?> metric(IgniteClient client, String metricName) {
        return metric(client, metricName, Collections.singleton(connectedTo(client)));
    }

    /**
     * @param client Client.
     * @param metricName Metric name.
     * @param nodeIds Node ids.
     */
    public static Map<String, ?> metric(IgniteClient client, String metricName, Set<UUID> nodeIds) {
        return (Map<String, ?>)executeTask(
            client,
            VisorMetricTask.class.getName(),
            new VisorMetricTaskArg(metricName),
            nodeIds);
    }

    /**
     * @param client Client.
     * @param metricName Metric name.
     */
    @Nullable public static Object metricValue(IgniteClient client, String metricName) {
        return metric(client, metricName)
            .values()
            .stream()
            .findFirst()
            .orElse(null);
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
            return client.compute().execute(taskCls, new VisorTaskArgument<>(nodeIds, arg, false));
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
        List<List<?>> allNodesAttrs = viewOfRandomNode(client, BASELINE_NODE_ATTRIBUTES);

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
     * @param otherServerNodes Other server nodes.
     */
    public static void groupServerNodesByState(IgniteClient client, Collection<ClusterNode> onlineBaselineNodes,
        Collection<OfflineNodeInfo> offlineBaselineNodes, Set<ClusterNode> otherServerNodes) {
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

        offlineBaselineNodes.addAll(offlineByConsistentIds(client, offlineConsistentIds));

        otherServerNodes.addAll(nonHandledNodes);
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
