/*
 * Copyright 2023 Ilya Shishkov (https://github.com/shishkovilja)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ignitop.ignite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import dev.ignitop.ignite.system.SystemMetricsInformation;
import dev.ignitop.ignite.topology.OfflineNodeInfo;
import dev.ignitop.ignite.topology.OnlineNodeInfo;
import dev.ignitop.ignite.topology.TopologyInformation;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientClusterGroup;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.internal.management.metric.MetricTask;
import org.apache.ignite.internal.management.metric.MetricCommandArg;
import org.apache.ignite.internal.management.SystemViewTask;
import org.apache.ignite.internal.management.SystemViewCommandArg;
import org.apache.ignite.internal.management.SystemViewTaskResult;
import org.apache.ignite.internal.visor.VisorTaskArgument;

import static dev.ignitop.util.IgniTopUtils.append;
import static org.apache.ignite.internal.IgniteKernal.CFG_VIEW;
import static org.apache.ignite.internal.processors.cache.persistence.DataRegionMetricsImpl.DATAREGION_METRICS_PREFIX;
import static org.apache.ignite.internal.processors.cache.persistence.DataStorageMetricsImpl.DATASTORAGE_METRIC_PREFIX;
import static org.apache.ignite.internal.processors.metric.GridMetricManager.CPU_LOAD;
import static org.apache.ignite.internal.processors.metric.GridMetricManager.GC_CPU_LOAD;
import static org.apache.ignite.internal.processors.metric.GridMetricManager.SYS_METRICS;
import static org.apache.ignite.internal.processors.metric.impl.MetricUtils.metricName;

/**
 *
 */
public class IgniteHelper implements AutoCloseable {
    /** Baseline node attributes system view. */
    public static final String BASELINE_NODE_ATTRIBUTES_VIEW = "BASELINE_NODE_ATTRIBUTES";

    /** Baseline nodes system view. */
    public static final String BASELINE_NODES_VIEW = "BASELINE_NODES";

    /** Topology version metric. */
    public static final String TOPOLOGY_VERSION_METRIC = "io.discovery.CurrentTopologyVersion";

    /** Rebalanced metric. */
    public static final String REBALANCED_METRIC = "cluster.Rebalanced";

    /** Data region metric regex. */
    public static final Pattern DATA_REGION_METRIC_REGEX =
        Pattern.compile("DataStorageConfiguration\\.DefaultDataRegionConfiguration\\.Name|" +
        "DataStorageConfiguration.DataRegionConfigurations\\[[0-9]+].Name");

    /** Client. */
    private final IgniteClient client;

    /**
     * @param addresses Addresses.
     */
    public IgniteHelper(String... addresses) {
        client = Ignition.startClient(new ClientConfiguration().setAddresses(addresses));
    }

    /**
     *
     */
    public TopologyInformation topologyInformation() {
        Set<OnlineNodeInfo> onlineBaselineNodes = new HashSet<>();
        Set<OfflineNodeInfo> offlineBaselineNodes = new HashSet<>();
        Set<OnlineNodeInfo> nonBaselineNodes = new HashSet<>();

        groupServerNodesByState(onlineBaselineNodes, offlineBaselineNodes, nonBaselineNodes);

        OnlineNodeInfo crd = coordinator();

        long topVer = singleMetric(TOPOLOGY_VERSION_METRIC, crd.nodeId(), -1L);
        boolean rebalanced = singleMetric(REBALANCED_METRIC, crd.nodeId(), false);

        return new TopologyInformation(
            onlineBaselineNodes,
            offlineBaselineNodes,
            nonBaselineNodes,
            clientNodes(),
            crd,
            topVer,
            clusterState(),
            rebalanced);
    }

    /**
     *
     */
    public Collection<SystemMetricsInformation> systemMetrics() {
        Collection<ClusterNode> nodes = client.cluster().forServers().nodes();

        List<SystemMetricsInformation> metricsInfo = new ArrayList<>(nodes.size());

        for (ClusterNode node : nodes) {
            UUID nodeId = node.id();

            Map<String, ?> sysMetrics = metric(SYS_METRICS, nodeId);

            double cpuLoad = (double)sysMetrics.get(metricName(SYS_METRICS, CPU_LOAD));

            double loadAverage = Double.parseDouble(String.valueOf(sysMetrics.get(metricName(SYS_METRICS,
                "SystemLoadAverage"))));

            double gcCpuLoad = (double)sysMetrics.get(metricName(SYS_METRICS, GC_CPU_LOAD));

            long heapUsed = (long)sysMetrics.get(metricName(SYS_METRICS, "memory", "heap", "used"));
            long heapMax = (long)sysMetrics.get(metricName(SYS_METRICS, "memory", "heap", "max"));

            long dataStorageSize = singleMetric(metricName(DATASTORAGE_METRIC_PREFIX, "StorageSize"), nodeId, 0L);

            SystemMetricsInformation sysMetricsInfo = new SystemMetricsInformation(
                node.consistentId(),
                node.hostNames(),
                cpuLoad * 100,
                // Strange double metric to string conversion in Ignite
                loadAverage,
                gcCpuLoad,
                (double)heapUsed / heapMax * 100,
                dataRegionUsagePercents(nodeId),
                (double)dataStorageSize / 1024 / 1024 / 1024);

            metricsInfo.add(sysMetricsInfo);
        }

        return metricsInfo;
    }

    /**
     * Get data regions utilization int percents on a specified node.
     *
     * @param nodeId Node id.
     */
    private Map<String, Double> dataRegionUsagePercents(UUID nodeId) {
        List<List<?>> cfg = view(CFG_VIEW, nodeId);

        Set<String> drNames = cfg.stream()
            .filter(l -> DATA_REGION_METRIC_REGEX.matcher(String.valueOf(l.get(0))).find())
            .map(l -> String.valueOf(l.get(1)))
            .collect(Collectors.toSet());

        Map<String, Double> dataRegionUsagesPercents = new HashMap<>();

        for (String drName : drNames) {
            long offheapUsedSize = singleMetric(metricName(DATAREGION_METRICS_PREFIX, drName, "OffheapUsedSize"),
                nodeId, 0L);

            long maxSize = singleMetric(metricName(DATAREGION_METRICS_PREFIX, drName, "MaxSize"), nodeId, 0L);

            dataRegionUsagesPercents.put(drName, (double)offheapUsedSize / maxSize * 100);
        }

        return dataRegionUsagesPercents;
    }

    /**
     * Return result of {@link SystemViewTask} execution for a node with a specified id.
     * Expected, that ID is a value, which corresponds to a value returned by a {@link ClusterNode#id()}.
     *
     * @param sysViewName System view name.
     * @param nodeId      Node Id.
     */
    private List<List<?>> view(String sysViewName, UUID nodeId) {
        return view(sysViewName, Set.of(nodeId))
            .getOrDefault(nodeId, List.of());
    }

    /**
     * Get full result of multi-node {@link SystemViewTask} execution groupped by node identifiers.
     *
     * @param sysViewName System view name.
     * @param nodeIds     Node ids.
     */
    private Map<UUID, List<List<?>>> view(String sysViewName, Set<UUID> nodeIds) {
        Optional<UUID> randomNodeOpt = nodeIds.stream().findFirst();

        if (randomNodeOpt.isEmpty())
            return Map.of();

        SystemViewCommandArg sysViewCmdArg = new SystemViewCommandArg();
        sysViewCmdArg.systemViewName(sysViewName);

        SystemViewTaskResult res = (SystemViewTaskResult)executeTask(
            SystemViewTask.class.getName(),
            sysViewCmdArg,
            randomNodeOpt.get());

        HashMap<UUID, List<List<?>>> map = new HashMap<>(res.rows());
        map.keySet().retainAll(nodeIds);

        return map;
    }

    /**
     * Return result of single-node {@link MetricCommandArg} execution for a node with a specified id.
     * Expected, that ID is a value, which corresponds to a value returned by a {@link ClusterNode#id()}.
     *
     * @param metricName Metric name.
     * @param nodeId Node id.
     */
    private Map<String, ?> metric(String metricName, UUID nodeId) {
        MetricCommandArg metricCmdArg = new MetricCommandArg();

        metricCmdArg.name(metricName);

        Map<String, ?> res = (Map<String, ?>)executeTask(
            MetricTask.class.getName(),
            metricCmdArg,
            nodeId);

        return res != null ? res : Map.of();
    }

    /**
     * Return first found entry for a specified metric name. Because {@link MetricCommandArg} can return multiple
     * values, it is expected, that this method is called for a single-value metric (not whole metric registry).
     *
     * @param metricName Metric name.
     * @param nodeId Node id.
     * @param dflt Default value.
     */
    private  <T> T singleMetric(String metricName, UUID nodeId, T dflt) {
        return metric(metricName, nodeId)
            .values()
            .stream()
            .map(obj -> (T)obj)
            .findFirst()
            .orElse(dflt);
    }

    /**
     * Execute task on a single node.
     *
     * @param taskCls Task class.
     * @param arg Argument.
     * @param nodeId Node id.
     */
    private Object executeTask(String taskCls, Object arg, UUID nodeId) {
        try {
            ClientClusterGroup clusterGrp = client.cluster().forNodeId(nodeId);

            return client.compute(clusterGrp).execute(taskCls, new VisorTaskArgument<>(nodeId, arg, false));
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get baseline nodes attributes.
     *
     * @param consistentIds Consistent ids.
     * @param attrs Attributes.
     */
    private Map<String, Map<String, Object>> baselineNodesAttributes(Collection<?> consistentIds, String... attrs) {
        List<List<?>> allNodesAttrs = view(BASELINE_NODE_ATTRIBUTES_VIEW, client.cluster().node().id());

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
     * Partition nodes by state: online, offline, or outside of baseline.
     *
     * @param onlineBaselineNodes Online baseline nodes.
     * @param offlineBaselineNodes Offline baseline nodes.
     * @param nonBaselineNodes Server nodes outside of baseline.
     */
    private void groupServerNodesByState(Collection<OnlineNodeInfo> onlineBaselineNodes,
        Collection<OfflineNodeInfo> offlineBaselineNodes, Set<OnlineNodeInfo> nonBaselineNodes) {
        ClientClusterGroup servers = client.cluster().forServers();

        List<List<?>> baselineNodesView = view(BASELINE_NODES_VIEW, servers.node().id());

        Set<ClusterNode> nonHandledNodes = new HashSet<>(servers.nodes());

        Set<Object> offlineConsistentIds = new HashSet<>();

        for (List<?> nodeInfo : baselineNodesView) {
            String consistentId = String.valueOf(nodeInfo.get(0));
            boolean online = (boolean)nodeInfo.get(1);

            Optional<ClusterNode> nodeOpt = nonHandledNodes.stream()
                .filter(n -> consistentId.equals(String.valueOf(n.consistentId())))
                .findFirst();

            if (nodeOpt.isPresent() && online) {
                onlineBaselineNodes.add(toNodeInfo(nodeOpt.get()));

                nonHandledNodes.remove(nodeOpt.get());
            }
            else
                offlineConsistentIds.add(consistentId);
        }

        offlineBaselineNodes.addAll(offlineByConsistentIds(offlineConsistentIds));

        nonBaselineNodes.addAll(nonHandledNodes.stream()
            .map(this::toNodeInfo)
            .collect(Collectors.toSet()));
    }

    /**
     * @param node Node.
     */
    private OnlineNodeInfo toNodeInfo(ClusterNode node) {
        return new OnlineNodeInfo(
            node,
            singleMetric("sys.UpTime", node.id(), -1L));
    }

    /**
     * Get offline nodes attributes by consistent ids.
     *
     * @param offlineConsistentIds Offline nodes consistent ids.
     */
    private Set<OfflineNodeInfo> offlineByConsistentIds(Collection<?> offlineConsistentIds) {
        Map<String, Map<String, Object>> attrsMap = baselineNodesAttributes(offlineConsistentIds,
            "TcpCommunicationSpi.comm.tcp.host.names", "TcpCommunicationSpi.comm.tcp.addrs");

        return attrsMap.entrySet()
            .stream()
            .map(e -> new OfflineNodeInfo(e.getKey(), e.getValue().get("TcpCommunicationSpi.comm.tcp.host.names"),
                e.getValue().get("TcpCommunicationSpi.comm.tcp.addrs")))
            .collect(Collectors.toSet());
    }

    /**
     *
     */
    // TODO: Is an oldest always a coordinator? With Zookeper SPI?
    private OnlineNodeInfo coordinator() {
        return toNodeInfo(client.cluster().forOldest().node());
    }

    /**
     *
     */
    private Set<OnlineNodeInfo> clientNodes() {
        return client.cluster().forClients()
            .nodes()
            .stream()
            .map(this::toNodeInfo)
            .collect(Collectors.toSet());
    }

    /**
     *
     */
    private ClusterState clusterState() {
        return client.cluster().state();
    }

    /** {@inheritDoc} */
    @Override public void close() {
        client.close();
    }
}
