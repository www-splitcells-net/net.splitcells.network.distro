/* SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro.java.acme;

public class PublicKeyCryptoConfig {
    public static PublicKeyCryptoConfig publicKeyCryptoConfig(byte[] privatePem, byte[] publicPem) {
        return new PublicKeyCryptoConfig(privatePem, publicPem);
    }

    private final byte[] privatePem;
    private final byte[] publicPem;

    private PublicKeyCryptoConfig(byte[] privatePem, byte[] publicPem) {
        this.privatePem = privatePem;
        this.publicPem = publicPem;
    }

    public byte[] privatePem() {
        return privatePem;
    }

    public byte[] publicPem() {
        return publicPem;
    }
}
