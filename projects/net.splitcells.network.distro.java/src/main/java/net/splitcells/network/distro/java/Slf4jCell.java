/* SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro.java;

import net.splitcells.dem.Dem;
import net.splitcells.dem.data.set.list.AppendableList;
import net.splitcells.dem.environment.Cell;
import net.splitcells.dem.environment.Environment;
import net.splitcells.dem.environment.config.ProgramName;
import net.splitcells.dem.environment.resource.Console;
import net.splitcells.dem.resource.communication.Sender;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static ch.qos.logback.classic.util.ContextInitializer.CONFIG_FILE_PROPERTY;
import static net.splitcells.dem.Dem.configWrite;

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
 */
public class Slf4jCell implements Cell {
    @Override public String groupId() {
        return "net.splitcells";
    }

    @Override public String artifactId() {
        return "network.distro.java";
    }

    @Override public void accept(Environment env) {
        final var programName = configWrite().configValue(ProgramName.class);
        System.setProperty("net.splitcells.dem.environment.config.ProgramName", programName.toLowerCase());
        System.setProperty(CONFIG_FILE_PROPERTY, "net/splitcells/network/distro/java/logback/config.xml");
        final var logger = LoggerFactory.getLogger(programName);
        configWrite().withConfigValue(Console.class
                , new Sender<>() {
                    @Override
                    public <R extends AppendableList<String>> R append(String arg) {
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
