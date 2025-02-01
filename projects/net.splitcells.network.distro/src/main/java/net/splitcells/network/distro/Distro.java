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
import net.splitcells.network.system.SystemCell;
import net.splitcells.website.server.Config;

import static net.splitcells.dem.Dem.configValue;
import static net.splitcells.dem.lang.tree.TreeI.tree;
import static net.splitcells.dem.utils.ExecutionException.execException;
import static net.splitcells.website.server.ProjectConfig.projectConfig;

public class Distro {
    public static void main(String... args) {
        Dem.process(() -> {
            service().start();
            Dem.waitIndefinitely();
        }, Distro::configurator);
    }

    public static void configurator(Environment env) {
        net.splitcells.network.distro.java.Distro.configurator(env);
    }

    /**
     * <p>Provides a config for users, that run the software locally, without access to the public.
     * It also helps users to get support by providing log files.
     * This is used for GUI applications for instance.</p>
     * <p>Logs are written in the user friendly CommonMark format.
     * Many websites have a nice rendering of CommonMark documents,
     * which in turn should improve the interactions with non technical users.</p>
     *
     * @param env Adapts the given config.
     */
    public static void configuratorForLocalUsers(Environment env) {
        net.splitcells.network.distro.java.Distro.configuratorForLocalUsers(env);
    }

    public static Service service() {
        return SystemCell.projectsRenderer(config()).httpServer();
    }

    /**
     * @return Provide a webserver for users running this software locally.
     * @see #configuratorForLocalUsers(Environment)
     */
    public static Service serviceForLocalUsers() {
        return net.splitcells.network.distro.java.Distro.serviceForLocalUsers();
    }

    /**
     * @return Provide a webserver configuration for users running this software locally.
     * @see #configuratorForLocalUsers(Environment)
     */
    public static Config configForLocalUsers() {
        return net.splitcells.network.distro.java.Distro.configForLocalUsers();
    }

    @Deprecated
    public static Config config() {
        return config(net.splitcells.network.distro.java.Distro.config());
    }

    public static Config config(Config arg) {
        return arg.withAdditionalProject(projectConfig("/", configValue(NetworkDistroFileSystem.class)));
    }
}
