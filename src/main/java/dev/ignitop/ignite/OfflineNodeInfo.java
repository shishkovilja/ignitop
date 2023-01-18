package dev.ignitop.ignite;

import java.util.Objects;

public class OfflineNodeInfo {
    /** Consistent id. */
    private final Object consistentId;

    /** Hostnames. */
    private final String hostNames;

    /** Addresses. */
    private final String addresses;

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
