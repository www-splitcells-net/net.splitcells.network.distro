/* SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro.java;

import net.splitcells.cin.text.CinTextFileSystem;
import net.splitcells.dem.Dem;
import net.splitcells.dem.environment.Cell;
import net.splitcells.dem.environment.Environment;
import net.splitcells.dem.lang.annotations.JavaLegacy;
import net.splitcells.network.community.NetworkCommunityFileSystem;
import net.splitcells.network.hub.NetworkHubFileSystem;
import net.splitcells.network.log.NetworkLogFileSystem;
import net.splitcells.network.media.NetworkMediaFileSystem;
import net.splitcells.network.presentations.NetworkPresentationsFileSystem;
import net.splitcells.network.system.SystemCell;
import net.splitcells.network.worker.via.java.NetworkWorkerLogFileSystem;
import net.splitcells.website.binaries.BinaryFileSystem;
import net.splitcells.website.server.Config;
import net.splitcells.website.server.ServerConfig;

import static net.splitcells.dem.Dem.*;
import static net.splitcells.dem.lang.tree.TreeI.tree;
import static net.splitcells.dem.utils.ExecutionException.execException;
import static net.splitcells.website.server.ProjectConfig.projectConfig;

@JavaLegacy
public class DistroCell implements Cell {
    public static void main(String... args) {
        Dem.serve(DistroCell.class);
    }

    @Override
    public String groupId() {
        return "net.splitcells";
    }

    @Override
    public String artifactId() {
        return "network.distro.java";
    }

    @Override
    public void accept(Environment env) {
        env.withCell(SystemCell.class);
        webConfig(env.config().configValue(ServerConfig.class));
        env.config().withConfigValue(NetworkLogFileSystem.class, env.config().configValue(NetworkWorkerLogFileSystem.class));
    }

    private Config webConfig(Config arg) {
        return arg
                .withAdditionalProject(projectConfig("/",
                        configValue(NetworkMediaFileSystem.class)))
                .withAdditionalProject(projectConfig("/"
                        , configValue(NetworkWorkerLogFileSystem.class)))
                .withAdditionalProject(projectConfig("/"
                        , configValue(NetworkLogFileSystem.class)))
                .withAdditionalProject(projectConfig("/"
                        , configValue(BinaryFileSystem.class)))
                .withAdditionalProject(projectConfig("/net/splitcells/network/community/"
                        , configValue(NetworkCommunityFileSystem.class)))
                .withAdditionalProjectAtStart(projectConfig("/net/splitcells/network/hub/"
                        , configValue(NetworkHubFileSystem.class)))
                .withAdditionalProject(projectConfig("/"
                        , configValue(NetworkPresentationsFileSystem.class)))
                .withAdditionalProject(projectConfig("/net/splitcells/cin/text/"
                        , configValue(CinTextFileSystem.class)))
                .withAdditionalProject(projectConfig("/"
                        , configValue(NetworkDistroJavaFileSystem.class)))
                .withAdditionalJsBackgroundFiles("net/splitcells/website/js/tabulator.min.js")
                .withAdditionalJsBackgroundFiles("net/splitcells/website/js/tabulator.injection.js")
                .withAdditionalJsBackgroundFiles("net/splitcells/website/js/jquery-fancytree-all-deps.min.js")
                .withAdditionalJsBackgroundFiles("net/splitcells/website/js/jquery-fancytree-all-deps-injection.min.js")
                .withAdditionalCssFile("net/splitcells/website/css/jquery-fancytree-all-deps.min.css")
                .withFrontMenuCommonMarkDescription(configValue(NetworkHubFileSystem.class)
                        .readString("src/main/md/net/splitcells/network/hub/README-for-users.md"))
                ;
    }
}
