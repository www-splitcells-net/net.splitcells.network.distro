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

import net.splitcells.cin.text.CinTextFileSystem;
import net.splitcells.dem.Dem;
import net.splitcells.dem.data.set.list.ListWA;
import net.splitcells.dem.environment.Environment;
import net.splitcells.dem.environment.config.ProgramName;
import net.splitcells.dem.environment.resource.Console;
import net.splitcells.dem.environment.resource.Service;
import net.splitcells.dem.lang.annotations.JavaLegacyArtifact;
import net.splitcells.dem.resource.communication.Sender;
import net.splitcells.dem.resource.communication.log.Logs;
import net.splitcells.dem.resource.communication.log.MessageFilter;
import net.splitcells.network.community.NetworkCommunityFileSystem;
import net.splitcells.network.distro.java.acme.CurrentAcmeAuthorization;
import net.splitcells.network.hub.NetworkHubFileSystem;
import net.splitcells.network.log.NetworkLogFileSystem;
import net.splitcells.network.media.NetworkMediaFileSystem;
import net.splitcells.network.presentations.NetworkPresentationsFileSystem;
import net.splitcells.system.WebsiteViaJar;
import net.splitcells.website.binaries.BinaryFileSystem;
import net.splitcells.website.server.Config;
import net.splitcells.website.server.projects.extension.ProjectsRendererExtensions;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

import static ch.qos.logback.classic.util.ContextInitializer.CONFIG_FILE_PROPERTY;
import static net.splitcells.dem.Dem.configValue;
import static net.splitcells.dem.Dem.environment;
import static net.splitcells.dem.lang.tree.TreeI.tree;
import static net.splitcells.dem.resource.communication.Sender.stringSender;
import static net.splitcells.dem.resource.communication.log.CommonMarkLogger.commonMarkDui;
import static net.splitcells.dem.resource.communication.log.LogLevel.TRACE;
import static net.splitcells.dem.utils.ExecutionException.executionException;
import static net.splitcells.network.distro.java.acme.AcmeChallengeFile.acmeChallengeFile;
import static net.splitcells.website.server.ProjectConfig.projectConfig;

@JavaLegacyArtifact
public class Distro {
    public static void main(String... args) {
        Dem.process(() -> {
            service().start();
            Dem.waitIndefinitely();
        }, Distro::configurator);
    }

    public static void configurator(Environment env) {
    }

    public static void ensureSslCertificatePresence(Environment env) {
        env.config()
                .withInitedOption(CurrentAcmeAuthorization.class)
                .configValue(ProjectsRendererExtensions.class)
                .withAppended(acmeChallengeFile());
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
            throw executionException(tree("Could not delete local log file.")
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
                .withAdditionalProject(projectConfig("/net/splitcells/network/community/"
                        , configValue(NetworkCommunityFileSystem.class)))
                .withAdditionalProjectAtStart(projectConfig("/net/splitcells/network/hub/"
                        , configValue(NetworkHubFileSystem.class)))
                .withAdditionalProject(projectConfig("/"
                        , configValue(NetworkPresentationsFileSystem.class)))
                .withAdditionalProject(projectConfig("/net/splitcells/cin/text/"
                        , configValue(CinTextFileSystem.class)))
                .withAdditionalJsBackgroundFiles("net/splitcells/website/js/tabulator.min.js")
                .withAdditionalJsBackgroundFiles("net/splitcells/website/js/tabulator.injection.js")
                .withAdditionalJsBackgroundFiles("net/splitcells/website/js/jquery-fancytree-all-deps.min.js")
                .withAdditionalJsBackgroundFiles("net/splitcells/website/js/jquery-fancytree-all-deps-injection.min.js")
                .withAdditionalCssFile("net/splitcells/website/css/jquery-fancytree-all-deps.min.css")
                ;
    }

    /**
     * <p>This method sets up a global config for logging via slf4j for the current class loader and
     * the given {@link Environment}.
     * This means, that this config is shared across all {@link Dem#process(Runnable, Consumer)}
     * instances by default.</p>
     * <p>slf4j is used in order to integrate logs of frameworks into the logging output.
     * Some frameworks detect the presence of slf4j and enable appropriate logging.</p>
     * <p>IDEA It may be a good idea, to provide one config for each {@link Dem#process(Runnable, Consumer)},
     * but no trivial method for starting a thread with a copy of the current class loader was found.
     * Instead of a copy of the current class loader,
     * a new class loader with the same type and arguments would also be enough.
     * In order to easy development, it was decided to just use one slf4j config for now,
     * and to implement more complex settings, when a concrete need for that arises.</p>
     * <p>IDEA Consider storing logs in database, so that SQL can be used for analysis:
     * https://stackoverflow.com/questions/59573185/springboot-to-store-logs-in-h2-db-logback-configuration-error-detected</p>
     *
     * @param env Adjust env to use slf4j as a {@link Console}.
     */
    public static void setGlobalUnixStateLogger(Environment env) {
        final var programName = env.config().configValue(ProgramName.class);
        System.setProperty("net.splitcells.dem.environment.config.ProgramName", programName);
        System.setProperty(CONFIG_FILE_PROPERTY, "net/splitcells/network/distro/java/logback/config.xml");
        final var logger = LoggerFactory.getLogger(programName);
        env.config().withConfigValue(Console.class
                , new Sender<>() {
                    @Override
                    public <R extends ListWA<String>> R append(String arg) {
                        logger.info(arg);
                        return (R) this;
                    }

                    @Override
                    public void close() {
                        // The logger API does not provide a close method.
                    }

                    @Override
                    public void flush() {
                        // The logger API does not provide a flush method.
                    }
                });
    }
}
