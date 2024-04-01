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
package net.splitcells.network.distro.java;

import net.splitcells.dem.Dem;
import net.splitcells.dem.environment.Environment;
import net.splitcells.dem.environment.resource.Console;
import net.splitcells.dem.environment.resource.Service;
import net.splitcells.dem.resource.communication.log.Logs;
import net.splitcells.dem.resource.communication.log.MessageFilter;
import net.splitcells.network.community.NetworkCommunityFileSystem;
import net.splitcells.network.hub.NetworkHubFileSystem;
import net.splitcells.network.log.NetworkLogFileSystem;
import net.splitcells.network.media.NetworkMediaFileSystem;
import net.splitcells.system.WebsiteViaJar;
import net.splitcells.website.binaries.BinaryFileSystem;
import net.splitcells.website.server.Config;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;

import static net.splitcells.dem.Dem.configValue;
import static net.splitcells.dem.Dem.environment;
import static net.splitcells.dem.lang.perspective.PerspectiveI.perspective;
import static net.splitcells.dem.resource.communication.Sender.stringSender;
import static net.splitcells.dem.resource.communication.log.CommonMarkLog.commonMarkDui;
import static net.splitcells.dem.resource.communication.log.LogLevel.TRACE;
import static net.splitcells.dem.utils.ExecutionException.executionException;
import static net.splitcells.website.server.ProjectConfig.projectConfig;

public class Distro {
    public static void main(String... args) {
        Dem.process(() -> {
            service().start();
            Dem.waitIndefinitely();
        }, Distro::configurator);
    }

    public static void configurator(Environment env) {
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
        configurator(env);
        env.config().withConfigValue(MessageFilter.class, logMessage -> logMessage.priority().greaterThan(TRACE));
        final var logFile = Path.of("./net.splitcells.network.distro.log.md");
        if (net.splitcells.dem.resource.Files.is_file(logFile)) {
            logFile.toFile().delete();
        }
        try {
            env.config().withConfigValue(Console.class
                    , stringSender(new FileOutputStream(logFile.toFile())));
        } catch (FileNotFoundException e) {
            throw executionException(perspective("Could not delete local log file.")
                    .withProperty("logFile", logFile.toString()), e);
        }
        env.config().withConfigValue(Logs.class, commonMarkDui(environment().config().configValue(Console.class)
                , environment().config().configValue(MessageFilter.class)));
    }

    public static Service service() {
        return WebsiteViaJar.projectsRenderer(config()).httpServer();
    }

    /**
     * @return Provide a webserver for users running this software locally.
     * @see #configuratorForLocalUsers(Environment)
     */
    public static Service serviceForLocalUsers() {
        return WebsiteViaJar.projectsRenderer(configForLocalUsers()).httpServer();
    }

    /**
     * @return Provide a webserver configuration for users running this software locally.
     * @see #configuratorForLocalUsers(Environment)
     */
    public static Config configForLocalUsers() {
        return config().withIsServerForGeneralPublic(false);
    }

    @Deprecated
    public static Config config() {
        return config(WebsiteViaJar.config());
    }

    public static Config config(Config arg) {
        return arg
                .withIsSecured(false)
                .withOpenPort(8443)
                .withAdditionalProject(projectConfig("/",
                        configValue(NetworkMediaFileSystem.class)))
                .withAdditionalProject(projectConfig("/"
                        , configValue(NetworkLogFileSystem.class)))
                .withAdditionalProject(projectConfig("/"
                        , configValue(BinaryFileSystem.class)))
                .withAdditionalProject(projectConfig("/"
                        , configValue(NetworkCommunityFileSystem.class)))
                .withAdditionalProjectAtStart(projectConfig("/net/splitcells/network/hub/"
                        , configValue(NetworkHubFileSystem.class)))
                ;
    }
}
