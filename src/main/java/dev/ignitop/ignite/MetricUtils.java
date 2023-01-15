package dev.ignitop.ignite;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.visor.VisorTaskArgument;
import org.apache.ignite.internal.visor.metric.VisorMetricTask;
import org.apache.ignite.internal.visor.metric.VisorMetricTaskArg;
import org.apache.ignite.internal.visor.systemview.VisorSystemViewTask;
import org.apache.ignite.internal.visor.systemview.VisorSystemViewTaskArg;
import org.apache.ignite.internal.visor.systemview.VisorSystemViewTaskResult;

public final class MetricUtils {
    public static VisorSystemViewTaskResult view(IgniteClient client, String viewName) {
        return view(client, viewName, Collections.singleton(connectedTo(client)));
    }

    public static VisorSystemViewTaskResult view(IgniteClient client, String viewName, Set<UUID> nodeIds) {
        VisorSystemViewTaskArg sysViewArg = new VisorSystemViewTaskArg(viewName);
        String clazz = VisorSystemViewTask.class.getName();

        try {
            return client.compute().execute(clazz, new VisorTaskArgument<>(allNodes(client), sysViewArg, false));
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<List<?>> findFirstInView(IgniteClient client, String viewName) {
        return view(client, viewName)
            .rows()
            .values()
            .stream()
            .findFirst()
            .orElse(Collections.emptyList());
    }

    public static Map<String, ?> metric(IgniteClient client, String metricName) {
        return metric(client, metricName, Collections.singleton(connectedTo(client)));
    }

    public static Map<String, ?> metric(IgniteClient client, String metricName, Set<UUID> nodeIds) {
        VisorMetricTaskArg taskArg = new VisorMetricTaskArg(metricName);
        String clazz = VisorMetricTask.class.getName();

        try {
            return client.compute().execute(clazz, taskArg);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<UUID> allNodes(IgniteClient client) {
        return client.cluster()
            .nodes()
            .stream()
            .map(ClusterNode::id)
            .collect(Collectors.toSet());
    }

    public static UUID connectedTo(IgniteClient client) {
        List<UUID> locNodes = client.cluster()
            .nodes()
            .stream()
            .filter(ClusterNode::isLocal)
            .map(ClusterNode::id)
            .collect(Collectors.toList());

        if (locNodes.size() == 1)
            return locNodes.get(0);
        else
            throw new IllegalStateException("Client should be connected exactly one node, " +
                "but Cluster API returned invalid amount: " + locNodes.size());
    }
}
