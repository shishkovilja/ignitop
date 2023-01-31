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

import java.util.Objects;

/**
 *
 */
public class OfflineNodeInfo {
    /** Consistent id. */
    private final Object consistentId;

    /** Hostnames. */
    private final String hostNames;

    /** Addresses. */
    private final String addresses;

    /**
     * @param consistentId Consistent id.
     * @param hostNames Host names.
     * @param addresses Addresses.
     */
    // TODO It seems, that we need extra constuctor with node attributes.
    public OfflineNodeInfo(Object consistentId, Object hostNames, Object addresses) {
        this.consistentId = consistentId;
        this.hostNames = String.valueOf(hostNames);
        this.addresses = String.valueOf(addresses);
    }

    /**
     * @return Consistent id.
     */
    public Object consistentId() {
        return consistentId;
    }

    /**
     * @return Hostnames.
     */
    public String hostNames() {
        return hostNames;
    }

    /**
     * @return Addresses.
     */
    public String addresses() {
        return addresses;
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        OfflineNodeInfo info = (OfflineNodeInfo)o;

        return Objects.equals(consistentId, info.consistentId) && Objects.equals(hostNames, info.hostNames) &&
            Objects.equals(addresses, info.addresses);
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return Objects.hash(consistentId, hostNames, addresses);
    }
}
