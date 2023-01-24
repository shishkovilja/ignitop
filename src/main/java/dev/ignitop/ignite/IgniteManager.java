package dev.ignitop.ignite;

import java.util.HashSet;
import java.util.Set;
import dev.ignitop.ignite.topology.OfflineNodeInfo;
import dev.ignitop.ignite.topology.OnlineNodeInfo;
import dev.ignitop.ignite.topology.TopologyInformation;
import dev.ignitop.ignite.util.IgniteClientHelper;

import static dev.ignitop.ignite.util.IgniteClientHelper.REBALANCED_METRIC;
import static dev.ignitop.ignite.util.IgniteClientHelper.TOPOLOGY_VERSION_METRIC;

/**
 *
 */
public class IgniteManager {
    /** Ignite client helper. */
    private final IgniteClientHelper igniteClientHelper;

    /**
     * @param igniteClientHelper Ignite client helper.
     */
    public IgniteManager(IgniteClientHelper igniteClientHelper) {
        this.igniteClientHelper = igniteClientHelper;
    }

    /**
     *
     */
    public TopologyInformation topologyInformation() {
        Set<OnlineNodeInfo> onlineBaselineNodes = new HashSet<>();
        Set<OfflineNodeInfo> offlineBaselineNodes = new HashSet<>();
        Set<OnlineNodeInfo> nonBaselineNodes = new HashSet<>();

        igniteClientHelper.groupServerNodesByState(onlineBaselineNodes, offlineBaselineNodes, nonBaselineNodes);

        OnlineNodeInfo crd = igniteClientHelper.coordinator();

        long topVer = igniteClientHelper.singleMetric(TOPOLOGY_VERSION_METRIC, crd.nodeId(), Long.class, -1L);
        boolean rebalanced = igniteClientHelper.singleMetric(REBALANCED_METRIC, crd.nodeId(), Boolean.class, false);

        return new TopologyInformation(
            onlineBaselineNodes,
            offlineBaselineNodes,
            nonBaselineNodes,
            igniteClientHelper.clientNodes(),
            crd,
            topVer,
            igniteClientHelper.clusterState(),
            rebalanced);
    }
}
