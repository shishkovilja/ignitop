package dev.ignitop.ignite;

public class OfflineNodeInfo {
    /** Consistent id. */
    private final Object consistentId;

    /** Hostnames. */
    private final String hostNames;

    /** Addresses. */
    private final String addresses;

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
}
