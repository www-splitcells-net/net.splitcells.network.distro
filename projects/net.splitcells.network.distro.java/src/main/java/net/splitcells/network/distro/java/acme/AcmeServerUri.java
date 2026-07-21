/* SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro.java.acme;

import net.splitcells.dem.environment.config.framework.Option;
import net.splitcells.dem.lang.tree.Tree;

import java.util.Optional;

import static net.splitcells.dem.lang.tree.TreeI.tree;

/**
 * For production the URI {@link #PRODUCTION_ACME_SERVER} might be used,
 * as the default URI is just for testing.
 */
public class AcmeServerUri implements Option<String> {
    public static final String PRODUCTION_ACME_SERVER = "https://acme-v02.api.letsencrypt.org/directory";
    @Override public String defaultValue() {
        return "https://acme-staging-v02.api.letsencrypt.org/directory";
    }
    @Override public Optional<Tree> serialize(String currentValue) {
        return Optional.of(tree(currentValue));
    }
}
