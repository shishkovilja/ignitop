package dev.ignitop.ui.updater.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import dev.ignitop.ignite.IgniteManager;
import dev.ignitop.ignite.topology.OfflineNodeInfo;
import dev.ignitop.ignite.topology.OnlineNodeInfo;
import dev.ignitop.ignite.topology.TopologyInformation;
import dev.ignitop.ui.component.TerminalComponent;
import dev.ignitop.ui.component.impl.EmptySpace;
import dev.ignitop.ui.component.impl.Header;
import dev.ignitop.ui.component.impl.Label;
import dev.ignitop.ui.component.impl.Table;
import dev.ignitop.ui.component.impl.Title;
import dev.ignitop.ui.updater.ScreenUpdater;

import static dev.ignitop.util.IgnitopUtils.formattedUptime;
import static org.apache.ignite.cluster.ClusterState.INACTIVE;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 *
 */
public class TopologyInformationUpdater implements ScreenUpdater {
    /** Ignite manager. */
    private final IgniteManager igniteMgr;

    /**
     * @param igniteMgr Ignite manager.
     */
    public TopologyInformationUpdater(IgniteManager igniteMgr) {
        this.igniteMgr = igniteMgr;
    }

    /** {@inheritDoc} */
    @Override public Collection<TerminalComponent> components() {
        ArrayList<TerminalComponent> components = new ArrayList<>();

        TopologyInformation topInfo = igniteMgr.topologyInformation();

        components.add(new Title("Topology"));

        components.add(Label.normal("Ignite version:")
            .bold(topInfo.coordinator().igniteVersion())
            .spaces(2)
            .normal("Coordinator:")
            .bold("[" + topInfo.coordinator().consistentId() + ',' + topInfo.coordinator().hostNames() + ']')
            .build());

        components.add(Label.normal("State:")
            .color(topInfo.clusterState() == INACTIVE ? RED : GREEN)
            .bold(topInfo.clusterState())
            .spaces(2)
            .normal("Topology version:")
            .bold(topInfo.topologyVersion())
            .spaces(2)
            .normal("Rebalanced:")
            .color(topInfo.rebalanced() ? GREEN : RED)
            .bold(topInfo.rebalanced())
            .build());

        components.add(new EmptySpace(2));

        addTable(components, "Online baseline nodes", nodesTable(topInfo.onlineBaselineNodes()));
        addTable(components, "Offline baseline nodes", offlineNodesTable(topInfo.offlineBaselineNodes()));
        addTable(components, "Non-baseline server nodes", nodesTable(topInfo.nonBaselineNodes()));
        addTable(components, "Client nodes", nodesTable(topInfo.clientNodes()));

        return Collections.unmodifiableList(components);
    }

    /**
     * Add table with header and surrounding empty spaces.
     *
     * @param components Components.
     * @param hdr Table header.
     * @param tbl Table.
     */
    private void addTable(List<TerminalComponent> components, String hdr, Table tbl) {
        components.add(new Header(hdr));
        components.add(tbl);
        components.add(new EmptySpace(2));
    }

    /**
     * @param nodes Nodes.
     */
    private Table nodesTable(Collection<OnlineNodeInfo> nodes) {
        List<String> hdr = List.of("Order", "Consistent ID", "Host names", "IP addresses", "Uptime");

        List<List<?>> rows = nodes.stream()
            .map(n -> List.of(
                n.order(),
                n.consistentId(),
                n.hostNames(),
                n.addresses(),
                formattedUptime(n.upTime())))
            .collect(Collectors.toList());

        return new Table(hdr, rows);
    }

    /**
     * @param nodes Nodes.
     */
    private Table offlineNodesTable(Collection<OfflineNodeInfo> nodes) {
        List<String> hdr = List.of("Consistent ID", "Host names", "IP addresses");

        List<List<?>> rows = nodes.stream()
            .map(n -> List.of(n.consistentId(), n.hostNames(), n.addresses()))
            .collect(Collectors.toList());

        return new Table(hdr, rows);
    }
}
