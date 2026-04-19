/* SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro.java;

import net.splitcells.dem.environment.Cell;
import net.splitcells.dem.environment.Environment;
import net.splitcells.dem.environment.resource.Console;
import net.splitcells.dem.resource.communication.log.Logs;
import net.splitcells.dem.resource.communication.log.MessageFilter;
import net.splitcells.dem.utils.ExecutionException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;

import static net.splitcells.dem.Dem.environment;
import static net.splitcells.dem.lang.tree.TreeI.tree;
import static net.splitcells.dem.resource.communication.Sender.stringSender;
import static net.splitcells.dem.resource.communication.log.CommonMarkLogger.commonMarkDui;
import static net.splitcells.dem.resource.communication.log.LogLevel.TRACE;

/**
 * <p>Provides a config for users, that run the software locally without being accessed by the public internet.
 * It also helps users to get support by providing log files.
 * This is used for GUI applications for instance.</p>
 * <p>Logs are written in the user-friendly CommonMark format.
 * Many websites have a nice rendering of CommonMark documents,
 * which in turn should improve the interactions with non-technical users.</p>
 */
public class LocalUserCell implements Cell {
    @Override public String groupId() {
        return "net.splitcells";
    }

    @Override public String artifactId() {
        return "network.distro.java";
    }

    @Override public void accept(Environment env) {
        env.config().withConfigValue(MessageFilter.class, logMessage -> logMessage.priority().greaterThan(TRACE));
        final var logFile = Path.of("./net.splitcells.network.distro.log.md");
        if (net.splitcells.dem.resource.Files.isFile(logFile)) {
            logFile.toFile().delete();
        }
        try {
            env.config().withConfigValue(Console.class
                    , stringSender(new FileOutputStream(logFile.toFile())));
        } catch (FileNotFoundException e) {
            throw ExecutionException.execException(tree("Could not delete local log file.")
                    .withProperty("logFile", logFile.toString()), e);
        }
        env.config().withConfigValue(Logs.class, commonMarkDui(environment().config().configValue(Console.class)
                , environment().config().configValue(MessageFilter.class)));
    }
}
