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

package dev.ignitop.ui.updater.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import dev.ignitop.ignite.IgniteHelper;
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

import static dev.ignitop.util.IgniTopUtils.formattedUptime;
import static org.apache.ignite.cluster.ClusterState.INACTIVE;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;

/**
 *
 */
public class TopologyInformationUpdater implements ScreenUpdater {
    /** Ignite helper. */
    private final IgniteHelper igniteHelper;

    /**
     * @param igniteHelper Ignite manager.
     */
    public TopologyInformationUpdater(IgniteHelper igniteHelper) {
        this.igniteHelper = igniteHelper;
    }

    /** {@inheritDoc} */
    @Override public Collection<TerminalComponent> components() {
        ArrayList<TerminalComponent> components = new ArrayList<>();

        TopologyInformation topInfo = igniteHelper.topologyInformation();

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

        List<Object[]> rows = nodes.stream()
            .map(n -> new Object[]{
                n.order(),
                n.consistentId(),
                n.hostNames(),
                n.addresses(),
                formattedUptime(n.upTime())})
            .collect(Collectors.toList());

        return new Table(hdr, rows);
    }

    /**
     * @param nodes Nodes.
     */
    private Table offlineNodesTable(Collection<OfflineNodeInfo> nodes) {
        List<String> hdr = List.of("Consistent ID", "Host names", "IP addresses");

        List<Object[]> rows = nodes.stream()
            .map(n -> new Object[]{n.consistentId(), n.hostNames(), n.addresses()})
            .collect(Collectors.toList());

        return new Table(hdr, rows);
    }
}
