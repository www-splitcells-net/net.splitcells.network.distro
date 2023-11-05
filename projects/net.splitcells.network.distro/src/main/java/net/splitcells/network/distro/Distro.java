/*
 * Copyright (c) 2021 Contributors To The `net.splitcells.*` Projects
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License v2.0 or later
 * which is available at https://www.gnu.org/licenses/old-licenses/gpl-2.0-standalone.html
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro;

import net.splitcells.dem.Dem;
import net.splitcells.dem.environment.Environment;
import net.splitcells.dem.environment.resource.Service;
import net.splitcells.dem.resource.FileSystemViaClassResourcesFactory;
import net.splitcells.system.WebsiteViaJar;
import net.splitcells.website.binaries.BinaryFileSystem;
import net.splitcells.website.server.Config;

import java.util.function.Consumer;

import static net.splitcells.dem.Dem.configValue;
import static net.splitcells.dem.resource.FileSystemViaClassResourcesAndSpringFactory.fileSystemViaClassResourcesAndSpringFactory;
import static net.splitcells.website.server.ProjectConfig.projectConfig;

public class Distro {
    public static void main(String... args) {
        Dem.process(() -> {
            service().start();
            Dem.waitIndefinitely();
        }, Distro::configurator);
    }

    public static void configurator(Environment env) {
        env.config().withConfigValue(FileSystemViaClassResourcesFactory.class
                , fileSystemViaClassResourcesAndSpringFactory());
    }

    public static Service service() {
        return WebsiteViaJar.projectsRenderer(config()).httpServer();
    }

    public static Config config() {
        return WebsiteViaJar.config()
                .withIsSecured(false)
                .withOpenPort(8443)
                .withAdditionalProject(projectConfig("/",
                        configValue(net.splitcells.network.media.FileSystem.class)))
                .withAdditionalProject(projectConfig("/"
                        , configValue(net.splitcells.network.log.FileSystem.class)))
                .withAdditionalProject(projectConfig("/"
                        , configValue(BinaryFileSystem.class)))
                .withAdditionalProject(projectConfig("/net/splitcells/network/community/"
                        , configValue(net.splitcells.network.community.FileSystem.class)))
                ;
    }
}
