/* SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro.java;

import net.splitcells.dem.environment.config.framework.Option;
import net.splitcells.dem.resource.FileSystemView;

import static net.splitcells.dem.resource.FileSystemViaClassResources.fileSystemViaClassResources;

public class NetworkDistroJavaFileSystem implements Option<FileSystemView> {
    @Override public FileSystemView defaultValue() {
        return fileSystemViaClassResources(NetworkDistroJavaFileSystem.class, "net.splitcells", "network.distro.java");
    }
}
