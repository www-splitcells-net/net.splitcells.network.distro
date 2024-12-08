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
package net.splitcells.network.distro.java.acme;

import net.splitcells.dem.lang.annotations.JavaLegacyArtifact;
import net.splitcells.dem.utils.StringUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Calendar;
import java.util.Date;

@JavaLegacyArtifact
public class SelfSignedPublicKeyCryptoConfigurator {
    private static final Provider SECURITY_PROVIDER = new BouncyCastleProvider();

    {
        Security.addProvider(SECURITY_PROVIDER);
    }

    public static SelfSignedPublicKeyCryptoConfigurator selfSignedPublicKeyCryptoConfigurator() {
        return new SelfSignedPublicKeyCryptoConfigurator();
    }

    private SelfSignedPublicKeyCryptoConfigurator() {

    }

    private final String defaultIdentityName = "anonymous";
    private final X500Name defaultIdentity = new X500Name("CN=" + defaultIdentityName);
    private final String signatureAlgorithm = "SHA256WithRSA";
    private final int keySize = 4096;

    public PublicKeyCryptoConfig selfSignedPublicKeyCryptoConfig() {
        final var selfSignedKeyPair = KeyPairUtils.createKeyPair(keySize);
        final var certificate = selfSignedCertificate(selfSignedKeyPair);
        final var privateKeyPem = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(privateKeyPem)) {
            pemWriter.writeObject(selfSignedKeyPair.getPrivate());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final var publicCertPem = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(publicCertPem)) {
            pemWriter.writeObject(certificate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return PublicKeyCryptoConfig.publicKeyCryptoConfig(StringUtils.toBytes(privateKeyPem.toString())
                , StringUtils.toBytes(publicCertPem.toString()));
    }

    public Certificate selfSignedCertificate(KeyPair keyPair) {
        try {
            final var now = System.currentTimeMillis();
            final var invalidBefore = new Date(now);
            final var certificateSerialNumber = new BigInteger(Long.toString(now));
            final var invalidAfterCalendar = Calendar.getInstance();
            invalidAfterCalendar.setTime(invalidBefore);
            invalidAfterCalendar.add(Calendar.DAY_OF_YEAR, 30);
            final var invalidAfter = invalidAfterCalendar.getTime();
            final var certificate = new JcaX509v3CertificateBuilder(defaultIdentity
                    , certificateSerialNumber
                    , invalidBefore
                    , invalidAfter
                    , defaultIdentity
                    , keyPair.getPublic())
                    .build(new JcaContentSignerBuilder(signatureAlgorithm).build(keyPair.getPrivate()));
            return new JcaX509CertificateConverter()
                    .setProvider(SECURITY_PROVIDER)
                    .getCertificate(certificate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
