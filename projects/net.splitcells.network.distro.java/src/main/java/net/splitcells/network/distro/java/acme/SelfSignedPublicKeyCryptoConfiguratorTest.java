/* SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro.java.acme;

import net.splitcells.dem.testing.annotations.UnitTest;

import static net.splitcells.network.distro.java.acme.SelfSignedPublicKeyCryptoConfigurator.selfSignedPublicKeyCryptoConfigurator;

public class SelfSignedPublicKeyCryptoConfiguratorTest {
    /**
     * This tests if any kind of public key can be generated without any errors by the underlying crypto library.
     */
    @UnitTest
    public void test() {
        selfSignedPublicKeyCryptoConfigurator().selfSignedPublicKeyCryptoConfig();
    }
}
