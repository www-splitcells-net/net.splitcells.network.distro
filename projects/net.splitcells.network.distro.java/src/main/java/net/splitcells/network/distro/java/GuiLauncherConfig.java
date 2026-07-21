/* SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro.java;

public class GuiLauncherConfig {
    public static GuiLauncherConfig guiLauncherConfig() {
        return new GuiLauncherConfig();
    }

    private String helpText = "";
    private String url = "";

    private GuiLauncherConfig() {

    }

    public String helpText() {
        return helpText;
    }

    public GuiLauncherConfig withHelpText(String arg) {
        helpText = arg;
        return this;
    }

    public String url() {
        return url;
    }

    public GuiLauncherConfig withUrl(String arg) {
        url = arg;
        return this;
    }
}
