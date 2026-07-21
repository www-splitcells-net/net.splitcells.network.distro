/* SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro.java.acme;

import net.splitcells.dem.environment.config.framework.Option;
import net.splitcells.dem.environment.config.framework.Variable;
import net.splitcells.dem.lang.annotations.JavaLegacy;
import net.splitcells.dem.lang.tree.Tree;
import org.shredzone.acme4j.Authorization;

import java.util.Optional;

import static net.splitcells.dem.environment.config.framework.Variable.variable;
import static net.splitcells.dem.lang.tree.TreeI.tree;

@JavaLegacy
public class CurrentAcmeAuthorization implements Option<Variable<Authorization>> {
    @Override public Variable<Authorization> defaultValue() {
        return variable();
    }
    @Override public Optional<Tree> serialize(Variable<Authorization> currentValue) {
        return Optional.empty();
    }
}
