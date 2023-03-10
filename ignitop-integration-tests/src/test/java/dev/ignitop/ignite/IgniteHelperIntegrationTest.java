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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import dev.ignitop.ignite.system.SystemMetricsInformation;
import dev.ignitop.ignite.topology.OfflineNodeInfo;
import dev.ignitop.ignite.topology.OnlineNodeInfo;
import dev.ignitop.ignite.topology.TopologyInformation;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.ClientConnectorConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.ThinClientConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.processors.metric.MetricRegistry;
import org.apache.ignite.spi.metric.LongMetric;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static java.util.Collections.emptySortedMap;
import static java.util.Comparator.comparing;
import static org.apache.ignite.cluster.ClusterState.ACTIVE;
import static org.apache.ignite.cluster.ClusterState.ACTIVE_READ_ONLY;
import static org.apache.ignite.cluster.ClusterState.INACTIVE;
import static org.apache.ignite.configuration.DataStorageConfiguration.DFLT_DATA_REG_DEFAULT_NAME;
import static org.apache.ignite.internal.processors.cache.persistence.DataRegionMetricsImpl.DATAREGION_METRICS_PREFIX;
import static org.apache.ignite.internal.processors.metric.impl.MetricUtils.metricName;
import static org.apache.ignite.internal.util.IgniteUtils.MB;

/**
 *
 */
@SuppressWarnings({"resource", "deprecation"})
@RunWith(Parameterized.class)
public class IgniteHelperIntegrationTest extends GridCommonAbstractTest {
    /** Servers count. */
    public static final int SERVERS_COUNT = 4;

    /** Addresses. */
    public static final String[] ADDRESSES = IntStream.range(0, SERVERS_COUNT)
        .mapToObj(i -> "127.0.0.1:" + (10800 + i))
        .toArray(String[]::new);

    /** Default region max size. */
    public static final long DEFAULT_REGION_MAX_SIZE = 128 * MB;

    /** First region. */
    public static final String FIRST_REGION = "first_region";

    /** Second region. */
    public static final String SECOND_REGION = "second_region";

    /** Data regions list. */
    public static final List<String> DATA_REGIONS = List.of(DFLT_DATA_REG_DEFAULT_NAME, FIRST_REGION, SECOND_REGION);

    /** First region size. */
    public static final long FIRST_REGION_SIZE = 256 * MB;

    /** Second region size. */
    public static final long SECOND_REGION_SIZE = 128 * MB;

    /** First region cache. */
    public static final String FIRST_REGION_CACHE = "first_region_cache";

    /** Second region cache. */
    public static final String SECOND_REGION_CACHE = "second_region_cache";

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
            .setDataStorageConfiguration(new DataStorageConfiguration()
                .setDefaultDataRegionConfiguration(
                    new DataRegionConfiguration()
                        .setMaxSize(DEFAULT_REGION_MAX_SIZE)
                        .setPersistenceEnabled(persistenceEnabled)
                        .setMetricsEnabled(true)
                )
                .setDataRegionConfigurations(
                    new DataRegionConfiguration()
                        .setName(FIRST_REGION)
                        .setPersistenceEnabled(false)
                        .setMaxSize(FIRST_REGION_SIZE)
                        .setMetricsEnabled(true),
                    new DataRegionConfiguration()
                        .setName(SECOND_REGION)
                        .setPersistenceEnabled(false)
                        .setMaxSize(SECOND_REGION_SIZE)
                        .setMetricsEnabled(true))
                .setMetricsEnabled(true))
            .setCacheConfiguration(
                new CacheConfiguration<>(DEFAULT_CACHE_NAME)
                    .setDataRegionName(DFLT_DATA_REG_DEFAULT_NAME),
                new CacheConfiguration<>(FIRST_REGION_CACHE)
                    .setDataRegionName(FIRST_REGION),
                new CacheConfiguration<>(SECOND_REGION_CACHE)
                    .setDataRegionName(SECOND_REGION));
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        cleanPersistenceDir();

        startGrids(SERVERS_COUNT);

        if (persistenceEnabled)
            grid(0).cluster().state(ACTIVE);

        awaitPartitionMapExchange();
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
    public void testTopologyInformation() throws Exception {
        try (IgniteHelper igniteHelper = new IgniteHelper(ADDRESSES)) {
            checkTopologyInfo(igniteHelper, SERVERS_COUNT, emptySortedMap(), 0, 0,
                ACTIVE, true);

            if (persistenceEnabled)
                checkPersistent(igniteHelper);
            else
                checkNonPersistent(igniteHelper);

            checkClusterStateChange(igniteHelper, ACTIVE_READ_ONLY, true);
            checkClusterStateChange(igniteHelper, INACTIVE, false);
            checkClusterStateChange(igniteHelper, ACTIVE, true);
        }
    }

    /**
     *
     */
    @Test
    public void testSystemMetrics() {
        try (IgniteHelper igniteHelper = new IgniteHelper(ADDRESSES)) {
            int itersCnt = 5;

            for (int i = 0; i < itersCnt; i++) {
                for (String cacheName : grid(0).cacheNames()) {
                    IgniteCache<Object, Object> cache = grid(0).cache(cacheName);

                    int cacheSz = cache.size(CachePeekMode.PRIMARY);

                    IntStream.range(cacheSz, cacheSz + 1024).forEach(j -> cache.put(j, j));
                }

                SortedSet<IgniteEx> sortedIgnites = ignitesByConsistentId(0, SERVERS_COUNT);

                SortedSet<SystemMetricsInformation> sysMetricsInfos = systemMetricsByConsistenId(igniteHelper);

                Iterator<SystemMetricsInformation> sysMetricsIter = sysMetricsInfos.iterator();

                for (IgniteEx ignite : sortedIgnites) {
                    SystemMetricsInformation sysMetrics = sysMetricsIter.next();

                    assertEquals("Unexpected 'consistentId'", ignite.localNode().consistentId(),
                        sysMetrics.consistentId());

                    assertTrue("Unexpected 'cpuLoadPercent'",
                        sysMetrics.cpuLoadPercent() >= 0 && sysMetrics.cpuLoadPercent() <= 100);

                    assertTrue("Unexpected 'loadAverage'", sysMetrics.loadAverage() >= 0);

                    assertTrue("Unexpected 'gcCpuLoadPercent'",
                        sysMetrics.gcCpuLoadPercent() >= 0 && sysMetrics.gcCpuLoadPercent() <= 100);

                    assertTrue("Unexpected 'heapUsagePercent'",
                        sysMetrics.heapUsagePercent() > 0 && sysMetrics.gcCpuLoadPercent() <= 100);

                    if (persistenceEnabled)
                        assertTrue("Unexpected storage size", sysMetrics.dataStorageSizeGigabytes() > 0);

                    checkDataRegions(ignite, sysMetrics);
                }

                assertFalse("Unprocessed SystemMetricsInformation was found", sysMetricsIter.hasNext());
            }
        }
    }

    /**
     * @param igniteHelper Ignite helper.
     */
    private void checkPersistent(IgniteHelper igniteHelper) throws Exception {
        SortedMap<String, List<String>> offlineInfos = new TreeMap<>();

        int stopCnt = 3;

        for (int i = 1; i <= stopCnt; i++) {
            int stopIdx = SERVERS_COUNT - i;

            ClusterNode stoppingNode = grid(stopIdx).localNode();

            offlineInfos.put(stoppingNode.consistentId().toString(), List.of(stoppingNode.hostNames().toString(),
                stoppingNode.addresses().toString()));

            stopGrid(stopIdx);

            checkTopologyInfo(igniteHelper, stopIdx, offlineInfos, 0, 0,
                ACTIVE, true);
        }

        int nonBaselineCnt = 2;

        startGridsMultiThreaded(SERVERS_COUNT, nonBaselineCnt);

        waitForTopology(SERVERS_COUNT - stopCnt + nonBaselineCnt);

        checkTopologyInfo(igniteHelper, SERVERS_COUNT - stopCnt, offlineInfos, nonBaselineCnt, 0,
            ACTIVE, true);

        int clientsCnt = 2;

        startClientGridsMultiThreaded(SERVERS_COUNT + nonBaselineCnt, clientsCnt);

        waitForTopology(SERVERS_COUNT - stopCnt + nonBaselineCnt + clientsCnt);

        checkTopologyInfo(igniteHelper, SERVERS_COUNT - stopCnt, offlineInfos, nonBaselineCnt, clientsCnt,
            ACTIVE, true);
    }

    /**
     * @param igniteHelper Ignite helper.
     */
    private void checkNonPersistent(IgniteHelper igniteHelper) throws Exception {
        int stopCnt = 3;

        for (int i = 1; i <= stopCnt; i++) {
            int stopIdx = SERVERS_COUNT - i;

            stopGrid(stopIdx);

            checkTopologyInfo(igniteHelper, stopIdx, emptySortedMap(), 0, 0,
                ACTIVE, true);
        }

        int clientsCnt = 2;

        startClientGridsMultiThreaded(SERVERS_COUNT - stopCnt, clientsCnt);

        waitForTopology(SERVERS_COUNT - stopCnt + clientsCnt);

        checkTopologyInfo(igniteHelper, SERVERS_COUNT - stopCnt, emptySortedMap(), 0, clientsCnt,
            ACTIVE, true);
    }

    /**
     * @param igniteHelper Ignite helper.
     * @param onlineCnt Expected online nodes count.
     * @param expOfflineInfo Expected offline node infos.
     * @param nonBaselineCnt Expected non-baseline nodes count.
     * @param clientsCnt Expected client nodes count.
     * @param clusterState Expected cluster state.
     * @param rebalanced Expected rebalanced state.
     */
    private void checkTopologyInfo(
        IgniteHelper igniteHelper,
        int onlineCnt,
        SortedMap<String, List<String>> expOfflineInfo,
        int nonBaselineCnt,
        int clientsCnt,
        ClusterState clusterState,
        boolean rebalanced)
    {
        int offlineEndExclusive = onlineCnt + expOfflineInfo.size();
        int nonBaselineEndExclusive = offlineEndExclusive + nonBaselineCnt;
        int clientsEndExclusive = nonBaselineEndExclusive + clientsCnt;

        TopologyInformation topInfo = igniteHelper.topologyInformation();

        assertEquals("Unexpected 'clusterState'", clusterState, topInfo.clusterState());
        assertEquals("Unexpected 'rebalanced'", rebalanced, topInfo.rebalanced());

        verifyOnlineNodes(ignitesByConsistentId(0, onlineCnt),
            onlineByConsistentId(topInfo.onlineBaselineNodes()), false);

        verifyOfflineNodes(expOfflineInfo, offlineByConsistentId(topInfo.offlineBaselineNodes()));

        verifyOnlineNodes(ignitesByConsistentId(offlineEndExclusive, nonBaselineEndExclusive),
            onlineByConsistentId(topInfo.nonBaselineNodes()), false);

        verifyOnlineNodes(ignitesByConsistentId(nonBaselineEndExclusive, clientsEndExclusive),
            onlineByConsistentId(topInfo.clientNodes()), true);
    }

    /**
     * @param igniteHelper Ignite helper.
     * @param clusterState Expected cluster state.
     * @param rebalanced Expected rebalanced state.
     */
    private void checkClusterStateChange(IgniteHelper igniteHelper, ClusterState clusterState, boolean rebalanced)
        throws InterruptedException {
        grid(0).cluster().state(clusterState);

        awaitPartitionMapExchange();

        TopologyInformation topInfo = igniteHelper.topologyInformation();

        assertEquals("Unexpected 'rebalanced' value", rebalanced, topInfo.rebalanced());
        assertEquals("Unexpected 'clusterState' value", clusterState, topInfo.clusterState());
    }

    /**
     * @param ignites Ignites.
     * @param infos Node infos.
     * @param clients Clients.
     */
    private void verifyOnlineNodes(SortedSet<IgniteEx> ignites, SortedSet<OnlineNodeInfo> infos, boolean clients) {
        Iterator<OnlineNodeInfo> nodeInfosIter = infos.iterator();

        for (IgniteEx ignite : ignites) {
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

    /**
     * Get sorted by a consistentId collection pf test ignite nodes with indexes within specified range.
     *
     * @param startInclusive Start inclusive.
     * @param endExclusive End exclusive.
     */
    private TreeSet<IgniteEx> ignitesByConsistentId(int startInclusive, int endExclusive) {
        return IntStream.range(startInclusive, endExclusive)
            .mapToObj(this::grid)
            .collect(Collectors.toCollection(() ->
                new TreeSet<>(comparing(ign -> String.valueOf(ign.localNode().consistentId())))));
    }

    /**
     * Get sorted by a consistentId collection of OnlineNodeInfo.
     *
     * @param infos Original collection of OnlineNodeInfo.
     */
    private SortedSet<OnlineNodeInfo> onlineByConsistentId(Collection<OnlineNodeInfo> infos) {
        SortedSet<OnlineNodeInfo> infosByConsistentId = new TreeSet<>(comparing(info -> String.valueOf(info.consistentId())));

        infosByConsistentId.addAll(infos);

        return infosByConsistentId;
    }

    /**
     * Get sorted by a consistentId collection of SystemMetricsInformation.
     *
     * @param igniteHelper Ignite helper.
     */
    private SortedSet<SystemMetricsInformation> systemMetricsByConsistenId(IgniteHelper igniteHelper) {
        Collection<SystemMetricsInformation> sysMetrics = igniteHelper.systemMetrics();

        SortedSet<SystemMetricsInformation> sysMetricsByConsistentId = new TreeSet<>(comparing(m ->
            String.valueOf(m.consistentId())));

        sysMetricsByConsistentId.addAll(sysMetrics);

        return sysMetricsByConsistentId;
    }

    /**
     * @param expOfflineInfo Expected offline node infos.
     * @param infos Actual OfflineNodeInfo collection.
     */
    private void verifyOfflineNodes(SortedMap<String, List<String>> expOfflineInfo, SortedSet<OfflineNodeInfo> infos) {
        Iterator<Map.Entry<String, List<String>>> expIter = expOfflineInfo.entrySet().iterator();
        Iterator<OfflineNodeInfo> nodeInfosIter = infos.iterator();

        while (expIter.hasNext()) {
            Map.Entry<String, List<String>> expInfo = expIter.next();
            OfflineNodeInfo nodeInfo = nodeInfosIter.next();

            assertEquals("Unexpected 'consistentId'", expInfo.getKey(), nodeInfo.consistentId());
            assertEquals("Unexpected 'hostNames'", expInfo.getValue().get(0), nodeInfo.hostNames());
            assertEquals("Unexpected 'addresses'", expInfo.getValue().get(1), nodeInfo.addresses());
        }

        assertFalse("Unproccessed OfflineNodeInfo was found", nodeInfosIter.hasNext());
    }

    /**
     * Get sorted by a consistentId collection of OfflineNodeInfo.
     *
     * @param infos Original coollection of OfflineNodeInfo.
     */
    private SortedSet<OfflineNodeInfo> offlineByConsistentId(Collection<OfflineNodeInfo> infos) {
        SortedSet<OfflineNodeInfo> infosByConsistentId = new TreeSet<>(comparing(info -> String.valueOf(info.consistentId())));

        infosByConsistentId.addAll(infos);

        return infosByConsistentId;
    }

    /**
     * @param ignite Ignite.
     * @param sysMetrics System metrics information.
     */
    private void checkDataRegions(IgniteEx ignite, SystemMetricsInformation sysMetrics) {
        Map<String, Double> drUsagePercents = sysMetrics.dataRegionUsagesPercents();

        assertEquals("Unexpected data regions count", DATA_REGIONS.size(), drUsagePercents.size());

        assertEqualsCollectionsIgnoringOrder(DATA_REGIONS, drUsagePercents.keySet());

        for (String drName : DATA_REGIONS) {
            MetricRegistry mReg = ignite.context().metric().registry(metricName(DATAREGION_METRICS_PREFIX, drName));

            long offheapUsedSize = ((LongMetric)mReg.findMetric("OffheapUsedSize")).value();
            long maxSize = ((LongMetric)mReg.findMetric("MaxSize")).value();

            double expUsagePercent = (double)offheapUsedSize / maxSize * 100;

            String errMsg = "Unexpected usage percent: [igniteName=" + ignite.name() + ", drName=" + drName + ']';

            assertEquals(errMsg, expUsagePercent, (double)drUsagePercents.get(drName));
        }
    }
}
