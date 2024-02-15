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

import static net.splitcells.network.distro.GuiLauncher.startGuiLauncher;
import static net.splitcells.network.distro.GuiLauncherConfig.guiLauncherConfig;

/**
 * <p>TODO FIX All external image links have to be redirected to a local resource or has to be blocked.
 * The most prominent example of this is the front image of the README in the `net.splitcells.network` project.
 * Otherwise, a real offline mode is not possible.</p>
 * <p>IDEA Create a electron like GUI, which may be a better system integration: https://github.com/cuba-labs/java-electron-tutorial</p>
 */
public class DistroGuiLauncher {
    /**
     * TODO The config should be done via {@link net.splitcells.dem.Dem} and not via a custom config system,
     * as this is a config for integrating the core program into the environment.
     * Therefore, these config should support a generic config system,
     * which could be configured by the caller.
     *
     * @param args
     */
    public static void main(String... args) {
        startGuiLauncher(guiLauncherConfig()
                .withUrl("http://localhost:8443/index.html")
                .withHelpText("This application's only GUI is this launcher. "
                        + "This application is a server program. "
                        + "Therefore, the program has to be accessed via an Internet browser: "
                        + "click on the `open` button or insert the `URL` into your favorite browser."));
        Dem.process(() -> {
            Distro.service().start();
            Dem.waitIndefinitely();
        }, Distro::configuratorForLocalUsers);
    }
}
