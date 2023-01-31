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

package dev.ignitop.ignite.topology;

import java.util.List;
import java.util.UUID;
import org.apache.ignite.cluster.ClusterNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.ignitop.util.TestUtils.IGNITE_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.when;

/**
 *
 */
class OnlineNodeInfoTest {
    /** Node. */
    private ClusterNode node;

    /**
     *
     */
    @BeforeEach
    void setUp() {
        node = Mockito.mock(ClusterNode.class);
    }

    /**
     *
     */
    @Test
    void nodeId() {
        UUID uuid = UUID.randomUUID();

        when(node.id()).thenReturn(uuid);

        assertEquals(uuid, new OnlineNodeInfo(node, 0).nodeId());
    }

    /**
     *
     */
    @Test
    void consistentId() {
        UUID uuid = UUID.randomUUID();

        when(node.consistentId()).thenReturn(uuid);

        assertEquals(uuid, new OnlineNodeInfo(node, 0).consistentId());
    }

    /**
     *
     */
    @Test
    void order() {
        when(node.order()).thenReturn(2L);

        assertEquals(2, new OnlineNodeInfo(node, 0).order());
    }

    /**
     *
     */
    @Test
    void igniteVersion() {
        when(node.version()).thenReturn(IGNITE_VERSION);

        assertEquals(IGNITE_VERSION, new OnlineNodeInfo(node, 0).igniteVersion());
    }

    /**
     *
     */
    @Test
    void hostNames() {
        List<String> hostNames = List.of("host1", "host2");

        when(node.hostNames()).thenReturn(hostNames);

        assertIterableEquals(hostNames, new OnlineNodeInfo(node, 0).hostNames());
    }

    /**
     *
     */
    @Test
    void addresses() {
        List<String> addreses = List.of("1.1.1.1", "2.2.2.2");

        when(node.addresses()).thenReturn(addreses);

        assertIterableEquals(addreses, new OnlineNodeInfo(node, 0).addresses());
    }

    /**
     *
     */
    @Test
    void upTime() {
        assertEquals(30, new OnlineNodeInfo(node, 30).upTime());
    }
}
