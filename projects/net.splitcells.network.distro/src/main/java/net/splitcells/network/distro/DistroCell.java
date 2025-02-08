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
import net.splitcells.dem.environment.Cell;
import net.splitcells.dem.environment.Environment;
import net.splitcells.dem.environment.resource.Service;
import net.splitcells.network.system.SystemCell;
import net.splitcells.website.server.Config;
import net.splitcells.website.server.WebsiteServerCell;

import static net.splitcells.dem.Dem.configValue;
import static net.splitcells.dem.Dem.serve;
import static net.splitcells.website.server.ProjectConfig.projectConfig;

public class DistroCell implements Cell {
    public static void main(String... args) {
        serve(DistroCell.class);
    }

    public static void deprecatedMain(String... args) {
        Dem.process(() -> {
            DistroCell.service().start();
            Dem.waitIndefinitely();
        }, DistroCell::configurator);
    }

    @Deprecated
    public static void configurator(Environment env) {
        net.splitcells.network.distro.java.Distro.configurator(env);
    }

    public void configurator2(Environment env) {
        configurator(env);
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
    @Deprecated
    public static void configuratorForLocalUsers(Environment env) {
        net.splitcells.network.distro.java.Distro.configuratorForLocalUsers(env);
    }

    public void configuratorForLocalUsers2(Environment env) {
        configuratorForLocalUsers(env);
    }

    @Deprecated
    public static Service service() {
        return SystemCell.projectsRenderer(config()).httpServer();
    }

    public Service service2() {
        return service();
    }

    /**
     * @return Provide a webserver for users running this software locally.
     * @see #configuratorForLocalUsers(Environment)
     */
    @Deprecated
    public static Service serviceForLocalUsers() {
        return net.splitcells.network.distro.java.Distro.serviceForLocalUsers();
    }

    public Service serviceForLocalUsers2() {
        return serviceForLocalUsers();
    }

    /**
     * @return Provide a webserver configuration for users running this software locally.
     * @see #configuratorForLocalUsers(Environment)
     */
    @Deprecated
    public static Config configForLocalUsers() {
        return net.splitcells.network.distro.java.Distro.configForLocalUsers();
    }

    public Config configForLocalUsers2() {
        return configForLocalUsers();
    }

    @Deprecated
    public static Config config() {
        return config(net.splitcells.network.distro.java.Distro.config());
    }

    public Config config2() {
        return config();
    }

    @Deprecated
    public static Config config(Config arg) {
        return arg.withAdditionalProject(projectConfig("/", configValue(NetworkDistroFileSystem.class)));
    }

    public Config config2(Config arg) {
        return config(arg);
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
        env.withCell(WebsiteServerCell.class);
    }
}
