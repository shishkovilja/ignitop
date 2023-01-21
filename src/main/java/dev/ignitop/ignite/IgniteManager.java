package dev.ignitop.ignite;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import dev.ignitop.ignite.topology.OfflineNodeInfo;
import dev.ignitop.ignite.topology.OnlineNodeInfo;
import dev.ignitop.ignite.topology.TopologyInformation;
import dev.ignitop.ignite.util.MetricUtils;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCluster;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.configuration.ClientConfiguration;

import static dev.ignitop.ignite.util.MetricUtils.groupServerNodesByState;
import static dev.ignitop.ignite.util.MetricUtils.singleMetric;
import static dev.ignitop.ignite.util.MetricUtils.toNodeInfo;

/**
 *
 */
public class IgniteManager implements AutoCloseable {
    /** Client. */
    private final IgniteClient client;

    /**
     * @param addresses Addresses.
     */
    public IgniteManager(String... addresses) {
        client = Ignition.startClient(new ClientConfiguration().setAddresses(addresses));
    }

    /** {@inheritDoc} */
    @Override public void close() {
        client.close();
    }

    /**
     *
     */
    public TopologyInformation topologyInformation() {
        Set<OnlineNodeInfo> onlineBaselineNodes = new HashSet<>();
        Set<OfflineNodeInfo> offlineBaselineNodes = new HashSet<>();
        Set<OnlineNodeInfo> nonBaselineNodes = new HashSet<>();

        groupServerNodesByState(client, onlineBaselineNodes, offlineBaselineNodes, nonBaselineNodes);

        ClientCluster cluster = client.cluster();

        // TODO: Is an oldest a coordinator?
        ClusterNode crd = cluster.forOldest().node();

        long topVer = singleMetric(client, MetricUtils.TOPOLOGY_VERSION_METRIC, crd.id(), Long.class, -1L);
        boolean rebalanced = singleMetric(client, MetricUtils.REBALANCED_METRIC, crd.id(), Boolean.class, false);

        Set<OnlineNodeInfo> clients = cluster.forClients()
            .nodes()
            .stream()
            .map(n -> toNodeInfo(client, n))
            .collect(Collectors.toSet());

        return new TopologyInformation(
            onlineBaselineNodes,
            offlineBaselineNodes,
            nonBaselineNodes,
            clients,
            toNodeInfo(client, crd),
            topVer,
            cluster.state(),
            rebalanced);
    }
}
