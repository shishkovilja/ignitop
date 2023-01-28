package dev.ignitop.ui.updater.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import dev.ignitop.ignite.IgniteManager;
import dev.ignitop.ignite.topology.OfflineNodeInfo;
import dev.ignitop.ignite.topology.OnlineNodeInfo;
import dev.ignitop.ignite.topology.TopologyInformation;
import dev.ignitop.ui.component.TerminalComponent;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.lang.IgniteProductVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.ignitop.util.TestUtils.renderToString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
class TopologyInformationUpdaterTest {
    /** Ignite version. */
    public static final IgniteProductVersion IGNITE_VERSION = new IgniteProductVersion((byte)2, (byte)15, (byte)0,
        0L, new byte[20]);

    /** Mock Ignite manager. */
    private IgniteManager igniteMgr;

    /** Online baseline nodes. */
    private List<OnlineNodeInfo> onlineBaselineNodes;

    /** Offline nodes. */
    private List<OfflineNodeInfo> offlineNodes;

    /** Non baseline nodes. */
    private List<OnlineNodeInfo> nonBaselineNodes;

    /** Client nodes. */
    private List<OnlineNodeInfo> clientNodes;

    /**
     *
     */
    @BeforeEach
    void setUp() {
        igniteMgr = mock(IgniteManager.class);

        onlineBaselineNodes = new ArrayList<>();
        offlineNodes = new ArrayList<>();
        nonBaselineNodes = new ArrayList<>();
        clientNodes = new ArrayList<>();
    }

    /**
     *
     */
    @Test
    void components_1OnlineNode_inactive_notRebalanced() {
        int topVer = 1;
        ClusterState clusterState = ClusterState.INACTIVE;
        boolean rebalanced = false;

        TopologyInformation topInfo = topologyInformation(1, 0, 0,
            0, topVer, clusterState, rebalanced);

        when(igniteMgr.topologyInformation()).thenReturn(topInfo);

        TopologyInformationUpdater topInfoUpdater = new TopologyInformationUpdater(igniteMgr);

        Iterator<TerminalComponent> iter = topInfoUpdater.components().iterator();

        assertTrue(renderToString(iter.next(), 400).contains("Topology"));

        TerminalComponent verAndCrdLbl = iter.next();

        assertTrue(renderToString(verAndCrdLbl, 400).contains("Ignite version: "));
        assertTrue(renderToString(verAndCrdLbl, 400).contains(IGNITE_VERSION.toString()));

        OnlineNodeInfo crd = onlineBaselineNodes.get(0);
        assertTrue(renderToString(verAndCrdLbl, 400).contains(String.valueOf(crd.consistentId())));
        assertTrue(renderToString(verAndCrdLbl, 400).contains(String.valueOf(crd.hostNames())));

        TerminalComponent topVerAndStateLbl = iter.next();

        assertTrue(renderToString(topVerAndStateLbl, 400).contains(String.valueOf(topVer)));
        assertTrue(renderToString(topVerAndStateLbl, 400).contains(String.valueOf(clusterState)));
        assertTrue(renderToString(topVerAndStateLbl, 400).contains(String.valueOf(rebalanced)));

        // TODO tables check
    }

    /**
     * @param onlineCnt Online nodes count.
     * @param offlineCnt Offline nodes count.
     * @param nonBaselineCnt Non-baseline nodes count.
     * @param clientsCnt Client nodes count.
     * @param topVer Topology version.
     * @param clusterState Cluster state.
     * @param rebalanced Rebalanced state.
     */
    private TopologyInformation topologyInformation(int onlineCnt, int offlineCnt, int nonBaselineCnt, int clientsCnt,
        long topVer, ClusterState clusterState, boolean rebalanced) {
        int i, j, k, l;

        for (i = 0; i < onlineCnt; i++)
            createAndAddOnlineNode(i, onlineBaselineNodes);

        for (j = i; j < i + offlineCnt; j++)
            createAndAddOfflineNode(j, offlineNodes);

        for (k = j; k < j + nonBaselineCnt; k++)
            createAndAddOnlineNode(k, nonBaselineNodes);

        for (l = 0; l < k + clientsCnt; l++)
            createAndAddOnlineNode(l, clientNodes);

        return new TopologyInformation(
            onlineBaselineNodes,
            offlineNodes,
            nonBaselineNodes,
            clientNodes,
            onlineBaselineNodes.get(0),
            topVer,
            clusterState,
            rebalanced);
    }

    /**
     * @param idx Index.
     * @param nodesCol Nodes collection.
     */
    private void createAndAddOnlineNode(int idx, Collection<OnlineNodeInfo> nodesCol) {
        OnlineNodeInfo nodeInfo = onlineNode(UUID.randomUUID(),
            idx,
            "node-" + idx,
            List.of("node-hostname-" + idx + "-1", "node-hostname-" + idx + "-2"),
            List.of("127.0.0." + idx),
            IGNITE_VERSION,
            200);

        nodesCol.add(nodeInfo);
    }

    /**
     * @param idx Index.
     * @param nodesCol Nodes collection.
     */
    private void createAndAddOfflineNode(int idx, Collection<OfflineNodeInfo> nodesCol) {
        var nodeInfo = new OfflineNodeInfo("node-" + idx,
            "[node-hostname-" + idx + "]",
            "[127.0.0." + idx + "]");

        nodesCol.add(nodeInfo);
    }

    /**
     * Create mock node information.
     *
     * @param id Id.
     * @param order Order.
     * @param consistentId Consistent id.
     * @param hostNames Host names.
     * @param addresses Addresses.
     * @param igniteProductVer Ignite product version.
     * @param uptime Uptime.
     */
    private OnlineNodeInfo onlineNode(
        UUID id,
        long order,
        Object consistentId,
        Collection<String> hostNames,
        Collection<String> addresses,
        IgniteProductVersion igniteProductVer,
        long uptime)
    {
        OnlineNodeInfo mockNode = mock(OnlineNodeInfo.class);

        when(mockNode.nodeId()).thenReturn(id);
        when(mockNode.order()).thenReturn(order);
        when(mockNode.consistentId()).thenReturn(consistentId);
        when(mockNode.hostNames()).thenReturn(hostNames);
        when(mockNode.addresses()).thenReturn(addresses);
        when(mockNode.igniteVersion()).thenReturn(igniteProductVer);
        when(mockNode.upTime()).thenReturn(uptime);

        return mockNode;
    }
}
