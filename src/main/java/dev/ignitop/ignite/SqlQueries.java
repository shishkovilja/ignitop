package dev.ignitop.ignite;

import java.util.List;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.client.IgniteClient;
import dev.ignitop.util.QueryResult;

/**
 *
 */
public final class SqlQueries {
    /** Cluster summary. */
    public static final String CLUSTER_SUMMARY = "select NAME, VALUE from SYS.METRICS " +
        "where name like 'cluster.%'";

    /** TopologyInformationUpdater version. */
    public static final String TOPOLOGY_VERSION = "select NAME, VALUE from SYS.METRICS " +
        "where name = 'io.discovery.CurrentTopologyVersion'";

    /** Coordinator. */
    public static final String COORDINATOR = "select NODE_ORDER, CONSISTENT_ID, HOSTNAMES, ADDRESSES, VERSION " +
        "from SYS.NODES " +
        "where NODE_ID = (" +
        "   select VALUE from SYS.METRICS " +
        "   where name ='io.discovery.Coordinator'" +
        ")";

    /** Online baseline nodes. */
    public static final String ONLINE_BASELINE_NODES = "select NODE_ORDER, CONSISTENT_ID, HOSTNAMES, ADDRESSES " +
        "from SYS.NODES " +
        "where CONSISTENT_ID in (select CONSISTENT_ID from SYS.BASELINE_NODES) " +
        "order by NODE_ORDER";

    /** Offline baseline nodes. */
    public static final String OFFLINE_BASELINE_NODES = "select h.NODE_CONSISTENT_ID, HOST_NAMES, IP_ADDRESSES " +
        "from (" +
        "   select NODE_CONSISTENT_ID, VALUE as HOST_NAMES " +
        "   from SYS.BASELINE_NODE_ATTRIBUTES " +
        "   where NAME = 'TcpCommunicationSpi.comm.tcp.host.names'" +
        ") as h " +
        "inner join (" +
        "   select NODE_CONSISTENT_ID, VALUE as IP_ADDRESSES " +
        "   from SYS.BASELINE_NODE_ATTRIBUTES " +
        "   where NAME = 'org.apache.ignite.ips'" +
        ") as ip " +
        "on h.NODE_CONSISTENT_ID = ip.NODE_CONSISTENT_ID " +
        "where h.NODE_CONSISTENT_ID in (" +
        "   select CONSISTENT_ID " +
        "   from SYS.BASELINE_NODES " +
        "   where online = 'false'" +
        ")";

    /** Client nodes. */
    public static final String CLIENT_NODES = "select NODE_ORDER, CONSISTENT_ID, HOSTNAMES, ADDRESSES " +
        "from SYS.NODES " +
        "where IS_CLIENT = 'true' " +
        "order by NODE_ORDER";

    /** Other nodes. */
    public static final String OTHER_NODES = "select NODE_ORDER, CONSISTENT_ID, HOSTNAMES, ADDRESSES " +
        "from SYS.NODES " +
        "where" +
        "   IS_CLIENT = 'false' and " +
        "   CONSISTENT_ID not in (select CONSISTENT_ID from SYS.BASELINE_NODES) " +
        "order by NODE_ORDER";


    /**
     * @param client Client.
     * @param sql SQL request.
     * @param args SQL arguments.
     */
    public static QueryResult executeQuery(IgniteClient client, String sql, Object... args) {
        try (FieldsQueryCursor<List<?>> qryCursor = client.query(new SqlFieldsQuery(sql).setArgs(args))) {
            return new QueryResult(qryCursor);
        }
    }

    /**
     * @param client Client.
     */
    public static QueryResult coordinator(IgniteClient client) {
        return executeQuery(client, COORDINATOR);
    }

    /**
     * @param client Client.
     */
    public static QueryResult topologyVersion(IgniteClient client) {
        return executeQuery(client, TOPOLOGY_VERSION);
    }

    /**
     * @param client Client.
     */
    public static QueryResult clusterSummary(IgniteClient client) {
        return executeQuery(client, CLUSTER_SUMMARY);
    }

    /**
     * @param client Client.
     */
    public static QueryResult onlineNodes(IgniteClient client) {
        return executeQuery(client, ONLINE_BASELINE_NODES);
    }

    /**
     * @param client Client.
     */
    public static QueryResult offlineNodes(IgniteClient client) {
        return executeQuery(client, OFFLINE_BASELINE_NODES);
    }

    /**
     * @param client Client.
     */
    public static QueryResult otherNodes(IgniteClient client) {
        return executeQuery(client, OTHER_NODES);
    }

    /**
     * @param client Client.
     */
    public static QueryResult clientNodes(IgniteClient client) {
        return executeQuery(client, CLIENT_NODES);
    }
}
