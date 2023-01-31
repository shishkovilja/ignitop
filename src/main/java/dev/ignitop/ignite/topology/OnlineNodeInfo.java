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

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.lang.IgniteProductVersion;

/**
 *
 */
public class OnlineNodeInfo {
    /** Node id. */
    private final UUID nodeId;

    /** Consitent id. */
    private final Object consistentId;

    /** Order. */
    private final long order;

    /** Ignite version. */
    private final IgniteProductVersion igniteVer;

    /** Hostnames. */
    private final Collection<String> hostNames;

    /** Addresses. */
    private final Collection<String> addresses;

    /** Uptime. */
    private final long upTime;

    /**
     * @param clusterNode Cluster node.
     * @param upTime Uptime.
     */
    public OnlineNodeInfo(ClusterNode clusterNode, long upTime) {
        order = clusterNode.order();
        nodeId = clusterNode.id();
        consistentId = clusterNode.consistentId();
        igniteVer = clusterNode.version();
        hostNames = clusterNode.hostNames();
        addresses = clusterNode.addresses();

        this.upTime = upTime;
    }

    /**
     *
     */
    public UUID nodeId() {
        return nodeId;
    }

    /**
     *
     */
    public Object consistentId() {
        return consistentId;
    }

    /**
     *
     */
    public long order() {
        return order;
    }

    /**
     *
     */
    public IgniteProductVersion igniteVersion() {
        return igniteVer;
    }

    /**
     *
     */
    public Collection<String> hostNames() {
        return Collections.unmodifiableCollection(hostNames);
    }

    /**
     *
     */
    public Collection<String> addresses() {
        return Collections.unmodifiableCollection(addresses);
    }

    /**
     *
     */
    public long upTime() {
        return upTime;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OnlineNodeInfo info = (OnlineNodeInfo)o;

        return nodeId.equals(info.nodeId);
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return Objects.hash(nodeId);
    }
}
