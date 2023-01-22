package dev.ignitop.ignite.topology;

import java.util.Collection;
import org.apache.ignite.cluster.ClusterState;

import static java.util.Collections.unmodifiableCollection;

/**
 *
 */
public class TopologyInformation {
    /** Online baseline nodes. */
    private final Collection<OnlineNodeInfo> onlineBaselineNodes;

    /** Offline baseline nodes. */
    private final Collection<OfflineNodeInfo> offlineBaselineNodes;

    /** Non baseline nodes. */
    private final Collection<OnlineNodeInfo> nonBaselineNodes;

    /** Client nodes. */
    private final Collection<OnlineNodeInfo> clientNodes;

    /** Crd. */
    private final OnlineNodeInfo crd;

    /** Topology version. */
    private final long topVer;

    /** Cluster state. */
    private final ClusterState clusterState;

    /** Rebalanced. */
    private final boolean rebalanced;

    /**
     * @param onlineBaselineNodes Online baseline nodes.
     * @param offlineBaselineNodes Offline baseline nodes.
     * @param nonBaselineNodes Non baseline nodes.
     * @param clientNodes Client nodes.
     * @param crd Crd.
     * @param topVer Topology version.
     * @param clusterState Cluster state.
     * @param rebalanced Rebalanced.
     */
    public TopologyInformation(
        Collection<OnlineNodeInfo> onlineBaselineNodes,
        Collection<OfflineNodeInfo> offlineBaselineNodes,
        Collection<OnlineNodeInfo> nonBaselineNodes,
        Collection<OnlineNodeInfo> clientNodes,
        OnlineNodeInfo crd,
        long topVer,
        ClusterState clusterState,
        boolean rebalanced)
    {
        this.onlineBaselineNodes = unmodifiableCollection(onlineBaselineNodes);
        this.offlineBaselineNodes = unmodifiableCollection(offlineBaselineNodes);
        this.nonBaselineNodes = unmodifiableCollection(nonBaselineNodes);
        this.clientNodes = unmodifiableCollection(clientNodes);
        this.crd = crd;
        this.topVer = topVer;
        this.clusterState = clusterState;
        this.rebalanced = rebalanced;
    }

    /**
     *
     */
    public Collection<OnlineNodeInfo> onlineBaselineNodes() {
        return onlineBaselineNodes;
    }

    /**
     *
     */
    public Collection<OfflineNodeInfo> offlineBaselineNodes() {
        return offlineBaselineNodes;
    }

    /**
     *
     */
    public Collection<OnlineNodeInfo> nonBaselineNodes() {
        return nonBaselineNodes;
    }

    /**
     *
     */
    public Collection<OnlineNodeInfo> clientNodes() {
        return clientNodes;
    }

    /**
     *
     */
    public OnlineNodeInfo coordinator() {
        return crd;
    }

    /**
     *
     */
    public long topologyVersion() {
        return topVer;
    }

    /**
     *
     */
    public ClusterState clusterState() {
        return clusterState;
    }

    /**
     *
     */
    public boolean rebalanced() {
        return rebalanced;
    }
}
