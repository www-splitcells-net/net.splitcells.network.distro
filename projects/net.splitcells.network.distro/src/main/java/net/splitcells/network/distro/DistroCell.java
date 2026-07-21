/* SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro;


import net.splitcells.dem.Dem;
import net.splitcells.dem.environment.Cell;
import net.splitcells.dem.environment.Environment;
import net.splitcells.dem.environment.resource.Service;
import net.splitcells.gel.ui.GelUiCell;
import net.splitcells.network.log.NetworkLogFileSystem;
import net.splitcells.network.system.SystemCell;
import net.splitcells.network.worker.via.java.NetworkWorkerLogFileSystem;
import net.splitcells.website.server.Config;
import net.splitcells.website.server.ServerConfig;
import net.splitcells.website.server.WebsiteServerCell;

import static net.splitcells.dem.Dem.configValue;
import static net.splitcells.dem.Dem.serve;
import static net.splitcells.website.server.ProjectConfig.projectConfig;

public class DistroCell implements Cell {
    public static void main(String... args) {
        serve(DistroCell.class);
    }

    @Override
    public String groupId() {
        return "net.splitcells";
    }

    @Override
    public String artifactId() {
        return "network.distro";
    }

    @Override
    public void accept(Environment env) {
        env.withCell(net.splitcells.network.distro.java.DistroCell.class);
        env.config().configValue(ServerConfig.class)
                .withAdditionalProject(projectConfig("/", configValue(NetworkDistroFileSystem.class)));
    }
}
