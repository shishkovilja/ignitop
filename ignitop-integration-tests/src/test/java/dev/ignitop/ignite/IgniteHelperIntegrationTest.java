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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import dev.ignitop.ignite.topology.OnlineNodeInfo;
import dev.ignitop.ignite.topology.TopologyInformation;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.ClientConnectorConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.ThinClientConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.apache.ignite.internal.util.IgniteUtils.MB;

/**
 *
 */
@SuppressWarnings("resource")
@RunWith(Parameterized.class)
public class IgniteHelperIntegrationTest extends GridCommonAbstractTest {
    /** Servers count. */
    public static final int SERVERS_COUNT = 4;

    /** Addresses. */
    public static final String[] ADDRESSES = IntStream.range(0, SERVERS_COUNT)
        .mapToObj(i -> "127.0.0.1:" + (10800 + i))
        .toArray(String[]::new);

    /** Persistence enabled. */
    @Parameter
    public boolean persistenceEnabled;

    /**
     *
     */
    @Parameters(name = "persistenceEnabled = {0}")
    public static Object[] parameters() {
        return new Object[]{true, false};
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        return super.getConfiguration(igniteInstanceName)
            .setConsistentId(igniteInstanceName)
            .setClientConnectorConfiguration(new ClientConnectorConfiguration()
            .setThinClientConfiguration(new ThinClientConfiguration()
                .setMaxActiveComputeTasksPerConnection(1)))
            .setDataStorageConfiguration(new DataStorageConfiguration().setDefaultDataRegionConfiguration(
                new DataRegionConfiguration()
                    .setMaxSize(256 * MB)
                    .setPersistenceEnabled(persistenceEnabled)));
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        cleanPersistenceDir();

        startGrids(SERVERS_COUNT);

        if (persistenceEnabled)
            grid(0).cluster().state(ClusterState.ACTIVE);
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        super.afterTest();

        stopAllGrids();

        cleanPersistenceDir();
    }

    /**
     *
     */
    @Test
    public void testTopologyInformation() {
        try (IgniteHelper igniteHelper = new IgniteHelper(ADDRESSES)) {
            checkTopologyInfo(igniteHelper, SERVERS_COUNT, SERVERS_COUNT, SERVERS_COUNT, SERVERS_COUNT,
                ClusterState.ACTIVE, true);
        }
    }

    /**
     * @param igniteHelper Ignite helper.
     * @param onlineEndExclusive Online end exclusive.
     * @param offlineEndExclusive Offline end exclusive.
     * @param nonBaselineEndExclusive Non baseline end exclusive.
     * @param clientsEndExclusive Clients end exclusive.
     * @param clusterState Cluster state.
     * @param rebalanced Rebalanced.
     */
    private void checkTopologyInfo(IgniteHelper igniteHelper, int onlineEndExclusive, int offlineEndExclusive,
        int nonBaselineEndExclusive, int clientsEndExclusive, ClusterState clusterState, boolean rebalanced) {
        TopologyInformation topInfo = igniteHelper.topologyInformation();

        assertEquals("Unexpected 'clusterState'", clusterState, topInfo.clusterState());

        assertEquals("Unexpected 'rebalanced'", rebalanced, topInfo.rebalanced());

        verifyOnlineNodes(ignitesByOrder(0, onlineEndExclusive),
            infosByOrder(topInfo.onlineBaselineNodes()), false);

//        verifyOnlineNodes(ignitesByOrder(onlineEndExclusive, offlineEndExclusive),
//            infosByOrder(topInfo.onlineBaselineNodes()), false);

        verifyOnlineNodes(ignitesByOrder(offlineEndExclusive, nonBaselineEndExclusive),
            infosByOrder(topInfo.nonBaselineNodes()), false);

        verifyOnlineNodes(ignitesByOrder(nonBaselineEndExclusive, clientsEndExclusive),
            infosByOrder(topInfo.clientNodes()), true);
    }

    /**
     * @param startInclusive Start inclusive.
     * @param endExclusive End exclusive.
     */
    private TreeSet<IgniteEx> ignitesByOrder(int startInclusive, int endExclusive) {
        return IntStream.range(startInclusive, endExclusive)
            .mapToObj(this::grid)
            .collect(Collectors.toCollection(() ->
                new TreeSet<>(Comparator.comparingLong(ign -> ign.localNode().order()))));
    }

    /**
     * @param nodeInfos Node infos.
     */
    private SortedSet<OnlineNodeInfo> infosByOrder(Collection<OnlineNodeInfo> nodeInfos) {
        SortedSet<OnlineNodeInfo> infosByOrder = new TreeSet<>(Comparator.comparingLong(OnlineNodeInfo::order));

        infosByOrder.addAll(nodeInfos);

        return infosByOrder;
    }

    /**
     * @param ignites Ignites.
     * @param nodeInfos Node infos.
     * @param clients Clients.
     */
    private void verifyOnlineNodes(SortedSet<IgniteEx> ignites, SortedSet<OnlineNodeInfo> nodeInfos, boolean clients) {
        Iterator<IgniteEx> ignitesIter = ignites.iterator();
        Iterator<OnlineNodeInfo> nodeInfosIter = nodeInfos.iterator();

        while (ignitesIter.hasNext()) {
            IgniteEx ignite = ignitesIter.next();
            OnlineNodeInfo nodeInfo = nodeInfosIter.next();

            ClusterNode expNode = ignite.localNode();

            assertEquals("Unexpected client mode", clients, expNode.isClient());
            assertEquals("Unexpected 'id", expNode.id(), nodeInfo.nodeId());
            assertEquals("Unexpected 'order'", expNode.order(), nodeInfo.order());
            assertEquals("Unexpected 'consistentId'", expNode.consistentId(), nodeInfo.consistentId());
            assertEquals("Unexpected 'igniteVersion'", expNode.version(), nodeInfo.igniteVersion());

            assertEqualsCollectionsIgnoringOrder(expNode.hostNames(), nodeInfo.hostNames());
            assertEqualsCollectionsIgnoringOrder(expNode.addresses(), nodeInfo.addresses());

            assertTrue("Unexpected 'upTime'", expNode.metrics().getUpTime() >= nodeInfo.upTime());
        }

        assertFalse("Unproccessed OnlineNodeInfo was found", nodeInfosIter.hasNext());
    }
}
