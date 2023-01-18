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
import org.jetbrains.annotations.Nullable;

public final class MetricUtils {
    public static VisorSystemViewTaskResult view(IgniteClient client, String viewName) {
        return view(client, viewName, Collections.singleton(connectedTo(client)));
    }

    public static VisorSystemViewTaskResult view(IgniteClient client, String viewName, Set<UUID> nodeIds) {
        return (VisorSystemViewTaskResult)executeTask(
            client,
            VisorSystemViewTask.class.getName(),
            new VisorSystemViewTaskArg(viewName),
            nodeIds);
    }

    public static List<List<?>> viewOfRandomNode(IgniteClient client, String viewName) {
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
        return (Map<String, ?>)executeTask(
            client,
            VisorMetricTask.class.getName(),
            new VisorMetricTaskArg(metricName),
            nodeIds);
    }

    @Nullable public static Object metricValue(IgniteClient client, String metricName) {
        return metric(client, metricName)
            .values()
            .stream()
            .findFirst()
            .orElse(null);
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
            throw new IllegalStateException("Client should be connected exactly to one node, " +
                "but Cluster API returned invalid amount: " + locNodes.size());
    }

    private static Object executeTask(IgniteClient client, String taskClazz, Object arg,
        Set<UUID> nodeIds) {
        try {
            return client.compute().execute(taskClazz, new VisorTaskArgument<>(nodeIds, arg, false));
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
