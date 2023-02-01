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
import java.util.Set;
import java.util.stream.Collectors;
import dev.ignitop.ignite.topology.OnlineNodeInfo;
import dev.ignitop.ignite.topology.TopologyInformation;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;

/**
 *
 */
public class IgniteHelperIntegrationTest extends GridCommonAbstractTest {
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        cfg.setConsistentId(igniteInstanceName);

        return cfg;
    }

    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        cleanPersistenceDir();
    }

    @Override protected void afterTest() throws Exception {
        super.afterTest();

        stopAllGrids();

        cleanPersistenceDir();
    }

    @Test
    public void testTopologyInformation() throws Exception {
        startGrids(2);

        waitForTopology(2);

        IgniteHelper igniteHelper = new IgniteHelper("127.0.0.1:10800", "127.0.0.1:10800");

        TopologyInformation topInfo = igniteHelper.topologyInformation();

        Collection<OnlineNodeInfo> onlineBaselineNodes = topInfo.onlineBaselineNodes();

        assertEquals(2, onlineBaselineNodes.size());

        assertEqualsCollectionsIgnoringOrder(
            Set.of(getClass() + "-1", getClass() + "-2"),
            onlineBaselineNodes.stream()
                .map(OnlineNodeInfo::consistentId)
                .collect(Collectors.toSet()));
    }
}
