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

package dev.ignitop.ui.updater.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import dev.ignitop.ignite.IgniteHelper;
import dev.ignitop.ignite.topology.OfflineNodeInfo;
import dev.ignitop.ignite.topology.OnlineNodeInfo;
import dev.ignitop.ignite.topology.TopologyInformation;
import dev.ignitop.ui.component.TerminalComponent;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.lang.IgniteProductVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.ignitop.util.IgniTopUtils.formattedUptime;
import static dev.ignitop.util.TestUtils.IGNITE_VERSION;
import static dev.ignitop.util.TestUtils.renderToString;
import static java.lang.System.lineSeparator;
import static org.apache.ignite.cluster.ClusterState.ACTIVE;
import static org.apache.ignite.cluster.ClusterState.ACTIVE_READ_ONLY;
import static org.apache.ignite.cluster.ClusterState.INACTIVE;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
class TopologyInformationUpdaterTest {
    /** Mock IgniteHelper. */
    private IgniteHelper igniteHelper;

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
        igniteHelper = mock(IgniteHelper.class);

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
        check(1, 0, 0, 0, 5, INACTIVE, false);
    }

    /**
     *
     */
    @Test
    void components_2online3ClientNodes_active_rebalanced() {
        check(2, 0, 0, 3, 7, ACTIVE, true);
    }

    /**
     *
     */
    @Test
    void components_5online2Offline4NonBaseline_activeReadOnly_notRebalanced() {
        check(5, 2, 4, 0, 21, ACTIVE_READ_ONLY, false);
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
    private void check(int onlineCnt, int offlineCnt, int nonBaselineCnt, int clientsCnt, int topVer,
        ClusterState clusterState, boolean rebalanced) {
        TopologyInformation topInfo = topologyInformation(onlineCnt, offlineCnt, nonBaselineCnt,
            clientsCnt, topVer, clusterState, rebalanced);

        when(igniteHelper.topologyInformation()).thenReturn(topInfo);

        TopologyInformationUpdater topInfoUpdater = new TopologyInformationUpdater(igniteHelper);

        Iterator<TerminalComponent> iter = topInfoUpdater.components().iterator();

        assertTrue(renderToString(iter.next(), 400).contains("Topology"));

        TerminalComponent verAndCrdLbl = iter.next();

        assertTrue(renderToString(verAndCrdLbl, 400).contains("Ignite version:"));
        assertTrue(renderToString(verAndCrdLbl, 400).contains(IGNITE_VERSION.toString()));

        OnlineNodeInfo crd = onlineBaselineNodes.get(0);
        assertTrue(renderToString(verAndCrdLbl, 400).contains(String.valueOf(crd.consistentId())));
        assertTrue(renderToString(verAndCrdLbl, 400).contains(String.valueOf(crd.hostNames())));

        checkTopologyAndStateLabel(iter.next(), topVer, clusterState, rebalanced);

        checkTable("Online baseline nodes", onlineCnt, topInfo::onlineBaselineNodes, this::validateOnlineNode, iter);
        checkTable("Offline baseline nodes", offlineCnt, topInfo::offlineBaselineNodes, this::validateOfflineNode, iter);
        checkTable("Non-baseline server nodes", nonBaselineCnt, topInfo::nonBaselineNodes, this::validateOnlineNode, iter);
        checkTable("Client nodes", clientsCnt, topInfo::clientNodes, this::validateOnlineNode, iter);
    }

    /**
     * @param component Component, topology version and state label expected.
     * @param topVer Topology version.
     * @param clusterState Cluster state.
     * @param rebalanced Rebalanced.
     */
    private static void checkTopologyAndStateLabel(TerminalComponent component, int topVer, ClusterState clusterState,
        boolean rebalanced) {
        String renderedLbl = renderToString(component, 400);

        String expStr = ansi().a("State:")
            .reset()
            .a(" ")
            .fg(clusterState == INACTIVE ? RED : GREEN)
            .bold()
            .a(clusterState)
            .reset()
            .a("  Topology version:")
            .reset()
            .a(" ")
            .bold()
            .a(topVer)
            .reset()
            .a("  Rebalanced:")
            .reset()
            .a(" ")
            .fg(rebalanced ? GREEN : RED)
            .bold()
            .a(rebalanced)
            .reset()
            .reset()
            .toString() + lineSeparator();

        assertEquals(expStr, renderedLbl, "Unexpected rendered cluster state label");
    }

    /**
     * Check table with header and empty spaces.
     *
     * @param hdr Table header.
     * @param rowsCnt Table rows count.
     * @param infoSupplier Info supplier.
     * @param rowValidator Row validator.
     * @param componentsIter Components iterator.
     */
    private <T> void checkTable(String hdr, int rowsCnt, Supplier<Collection<T>> infoSupplier, BiConsumer<String, T> rowValidator,
        Iterator<TerminalComponent> componentsIter) {
        // Empty space
        assertEquals(lineSeparator().repeat(2), renderToString(componentsIter.next(), 400));

        assertTrue(renderToString(componentsIter.next(), 400).contains(hdr));

        checkTableRows(rowsCnt, componentsIter.next(), infoSupplier.get(), rowValidator);
    }

    /**
     * @param tblRowsCnt Table rows count.
     * @param component TerminalProvider component. <em>Table</em> expected.
     * @param infos Nodes information collection.
     */
    private <T> void checkTableRows(int tblRowsCnt, TerminalComponent component, Collection<T> infos,
        BiConsumer<String, T> tblRowValidator) {
        List<String> renderedLines = renderToString(component, 400).lines()
            .collect(Collectors.toList());

        assertEquals(tblRowsCnt, renderedLines.size() - 2);

        Iterator<T> infoIter = infos.iterator();

        for (int i = 0; i < tblRowsCnt; i++) {
            String tblRow = renderedLines.get(i + 1);

            T nodeInfo = infoIter.next();

            tblRowValidator.accept(tblRow, nodeInfo);
        }

        assertFalse(infoIter.hasNext(), "Tables information should be empty");

        assertTrue(renderedLines.get(tblRowsCnt + 1).contains("Total items: " + tblRowsCnt),
            "Unexpected total elements in table");
    }

    /**
     * @param tblRow Table row.
     * @param info Table info.
     */
    public void validateOnlineNode(String tblRow, OnlineNodeInfo info) {
        assertTrue(tblRow.contains(String.valueOf(info.consistentId())), "Unexpected consistentId");
        assertTrue(tblRow.contains(String.valueOf(info.hostNames())), "Unexpected hostNames");
        assertTrue(tblRow.contains(String.valueOf(info.addresses())), "Unexpected addresses");
        assertTrue(tblRow.contains(String.valueOf(formattedUptime(info.upTime()))), "Unexpected upTime");
    }

    /**
     * @param tblRow Table row.
     * @param info Table info.
     */
    public void validateOfflineNode(String tblRow, OfflineNodeInfo info) {
        assertTrue(tblRow.contains(String.valueOf(info.consistentId())), "Unexpected consistentId");
        assertTrue(tblRow.contains(String.valueOf(info.hostNames())), "Unexpected hostNames");
        assertTrue(tblRow.contains(String.valueOf(info.addresses())), "Unexpected addresses");
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
            createAndAddOnlineNode(i, "online", onlineBaselineNodes);

        for (j = i; j < i + offlineCnt; j++)
            createAndAddOfflineNode(j, offlineNodes);

        for (k = j; k < j + nonBaselineCnt; k++)
            createAndAddOnlineNode(k, "other", nonBaselineNodes);

        for (l = k; l < k + clientsCnt; l++)
            createAndAddOnlineNode(l, "client", clientNodes);

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
    private void createAndAddOnlineNode(int idx, String prefix, Collection<OnlineNodeInfo> nodesCol) {
        OnlineNodeInfo nodeInfo = onlineNode(UUID.randomUUID(),
            idx,
            prefix + '-' + idx,
            List.of(prefix + "-hostname-" + idx + "-1", prefix + "-hostname-" + idx + "-2"),
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
        var nodeInfo = new OfflineNodeInfo("offline-" + idx,
            "[offline-hostname-" + idx + "]",
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
