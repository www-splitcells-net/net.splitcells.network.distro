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
import net.splitcells.dem.resource.ConfigFileSystem;
import net.splitcells.dem.resource.communication.log.LogLevel;
import net.splitcells.dem.utils.ExecutionException;
import net.splitcells.dem.utils.StringUtils;
import net.splitcells.website.server.config.PublicContactEMailAddress;
import net.splitcells.website.server.config.PublicDomain;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.util.KeyPairUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static net.splitcells.dem.Dem.configValue;
import static net.splitcells.dem.Dem.sleepAtLeast;
import static net.splitcells.dem.data.set.Sets.setOfUniques;
import static net.splitcells.dem.lang.tree.TreeI.tree;
import static net.splitcells.dem.resource.Files.fileExists;
import static net.splitcells.dem.resource.Files.readFileAsBytes;
import static net.splitcells.dem.resource.communication.log.Logs.logs;
import static net.splitcells.dem.utils.ExecutionException.execException;
import static net.splitcells.dem.utils.NotImplementedYet.notImplementedYet;

/**
 * One can use `https://letsdebug.net/` in order to debug `https://letsencrypt.org/`.
 * Published certificates can be found via `https://crt.sh/`.
 */
@JavaLegacyArtifact
public class PublicKeyCryptoConfigurator {
    private static final String CONFIG_PATH = "net/splitcells/network/distro/java/acme/publickeycryptoconfigurator";

    {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static PublicKeyCryptoConfig publicKeyCryptoConfig() {
        return publicKeyCryptoConfig(configValue(PublicDomain.class).orElseThrow()
                , configValue(PublicContactEMailAddress.class).orElseThrow());
    }

    private static PublicKeyCryptoConfig publicKeyCryptoConfig(String domain, String email) {
        final var publicKeyCryptoConfig = new PublicKeyCryptoConfigurator(email).publicKeyCryptoConfig(domain);
        logs().append(tree("Using the following certificate PEM:")
                        .withProperty("public certificate chain", StringUtils.parseString(publicKeyCryptoConfig.publicPem()))
                        .withProperty("private key", StringUtils.parseString(publicKeyCryptoConfig.privatePem()))
                , LogLevel.DEBUG);
        return publicKeyCryptoConfig;
    }

    private static final long TIME_BETWEEN_CHECKS = 3l;
    private final String sessionUrl = configValue(AcmeServerUri.class);
    private final String email;

    private static String[] configPath(String... path) {
        final var configPath = new ArrayList<String>();
        configPath.addAll(Arrays.asList(CONFIG_PATH.split("/")));
        configPath.addAll(Arrays.asList(path));
        return configPath.toArray(new String[configPath.size()]);
    }

    /**
     * <p>This is the private key, identifying the account used at {@link #sessionUrl},
     * in order to publicly sign {@link #acmeCertificatePath}.</p>
     * <p>TODO Create portable file storage concept.</p>
     */
    private final Path userKeyPairPath = configValue(ConfigFileSystem.class)
            .javaLegacyPath(Path.of("./", configPath("user-key-pair")))
            .orElseThrow();

    /**
     * <p>This is the private key used,
     * in order to encrypt the messages of the server,
     * that can be decrypt via {@link #acmeCertificatePath}.</p>
     * <p>TODO Create portable file storage concept.</p>
     */
    private final Path domainKeyPairPath = configValue(ConfigFileSystem.class)
            .javaLegacyPath(Path.of("./", configPath("domain-key-pair")))
            .orElseThrow();
    /**
     * <p>This is the public certificate, that can be used,
     * in order to authenticate the identity of the server.</p>
     * <p>TODO Create portable file storage concept.</p>
     */
    private final Path acmeCertificatePath = configValue(ConfigFileSystem.class)
            .javaLegacyPath(Path.of("./", configPath("certificate.pem")))
            .orElseThrow();

    private PublicKeyCryptoConfigurator(String emailArg) {
        email = emailArg;
        configValue(ConfigFileSystem.class).createDirectoryPath(CONFIG_PATH);
    }

    /**
     *
     * @param domain
     * @return Reads the crypto config from the {@link #acmeCertificatePath} on the file system or requests a new one.
     */
    public PublicKeyCryptoConfig publicKeyCryptoConfig(String domain) {
        try {
            if (fileExists(acmeCertificatePath)) {
                final var targetStream = new ByteArrayInputStream(readFileAsBytes(acmeCertificatePath));
                final var x509certificate = (X509Certificate) CertificateFactory
                        .getInstance("X509")
                        .generateCertificate(targetStream);
                final var currentTime = new Date();
                try {
                    x509certificate.checkValidity(currentTime);
                    return PublicKeyCryptoConfig.publicKeyCryptoConfig(readFileAsBytes(domainKeyPairPath)
                            , readFileAsBytes(acmeCertificatePath));
                } catch (Throwable t2) {
                    logs().appendWarning(tree("Certificate is invalid, according to the start, end and current time.")
                                    .withProperty("notBefore", "" + x509certificate.getNotBefore())
                                    .withProperty("notAfter", "" + x509certificate.getNotAfter())
                                    .withProperty("current time", "" + currentTime)
                            , t2);
                }
            }
            final var userKeyPair = userKeyPair();
            final var domainKeyPair = domainKeyPair();
            final var session = new Session(sessionUrl);
            final var account = account(session, userKeyPair);
            final var certificate = requestCertificate(domain, account, domainKeyPair);
            try (FileWriter fw = new FileWriter(acmeCertificatePath.toFile())) {
                certificate.writeCertificate(fw);
            }
            final var certificateStream = new ByteArrayOutputStream();
            final var certificateWriter = new OutputStreamWriter(certificateStream);
            try {
                certificate.writeCertificate(certificateWriter);
                certificateWriter.flush();
            } catch (IOException e) {
                throw execException(e);
            }
            return PublicKeyCryptoConfig.publicKeyCryptoConfig(readFileAsBytes(domainKeyPairPath), certificateStream.toByteArray());
        } catch (Throwable t) {
            throw execException(t);
        }
    }

    private org.shredzone.acme4j.Certificate requestCertificate(String domain, Account account, KeyPair domainKeyPair) {
        try {
            final var order = account.newOrder().domain(domain).create();
            order.getAuthorizations().forEach(this::authorize);
            order.execute(domainKeyPair);
            // From experience this can take a lot of time. So any time limit, does not make any sense for now.
            int i = 0;
            while (true) {
                ++i;
                logs().append(tree("Waiting for `" + sessionUrl + "` to provide certificate.")
                                .withProperty("status", order.getStatus().toString())
                                .withProperty("error", order.getError().map(e -> e.toString()).orElse("No error is present."))
                                .withProperty("status check count", "" + i)
                        , LogLevel.INFO);
                if (Status.INVALID.equals(order.getStatus())) {
                    throw ExecutionException.execException("Creating the certificate failed.");
                }
                if (Status.VALID.equals(order.getStatus())) {
                    return order.getCertificate();
                }
                final var now = Instant.now();
                final var updateTime = order.fetch().orElseGet(() -> Instant.now().plusSeconds(TIME_BETWEEN_CHECKS));
                final var waitDuration = now.until(updateTime, ChronoUnit.MILLIS);
                logs().append("Waiting "
                                + waitDuration
                                + " milliseconds for certificate to be provided by `"
                                + sessionUrl
                                + "`."
                        , LogLevel.INFO);
                sleepAtLeast(waitDuration);
            }
        } catch (Throwable t) {
            throw execException(t);
        }
    }

    private void authorize(Authorization auth) {
        try {
            if (Status.VALID.equals(auth.getStatus())) {
                return;
            }
            configValue(CurrentAcmeAuthorization.class).withValue(Optional.of(auth));
            final var challenge = auth.findChallenge(Http01Challenge.class).orElseThrow();
            logs().append(tree("Waiting for `" + sessionUrl + "` to execute the challenge.")
                            .withProperty("token", challenge.getToken())
                            .withProperty("identifier", auth.getIdentifier().getValue())
                            .withProperty("type of identifier", auth.getIdentifier().getType())
                    , LogLevel.INFO);
            challenge.trigger();
            // From experience this can take a lot of time. So any time limit, does not make any sense for now.
            int i = 0;
            while (true) {
                ++i;
                logs().append(tree("Waiting for `" + sessionUrl + "` to execute the challenge.")
                                .withProperty("status", challenge.getStatus().toString())
                                .withProperty("error", challenge.getError().map(e -> e.toString())
                                        .orElse("No error is present."))
                                .withProperty("status check count", "" + i)
                        , LogLevel.INFO);
                if (Status.INVALID.equals(challenge.getStatus())) {
                    throw ExecutionException.execException(tree("Could not complete ACME challenge.")
                            .withProperty("problem", challenge.getError().map(p -> p.toString()).orElse("No problem.")));
                }
                if (Status.VALID.equals(challenge.getStatus())) {
                    return;
                }
                final var now = Instant.now();
                final var updateTime = challenge.fetch().orElseGet(() -> Instant.now().plusSeconds(TIME_BETWEEN_CHECKS));
                final var waitDuration = now.until(updateTime, ChronoUnit.MILLIS);
                logs().append("Waiting "
                                + waitDuration
                                + " milliseconds for challenge update from `"
                                + sessionUrl
                                + "`."
                        , LogLevel.INFO);
                sleepAtLeast(waitDuration);
            }
        } catch (Throwable t) {
            throw execException(t);
        }
    }

    public KeyPair userKeyPair() {
        if (fileExists(userKeyPairPath)) {
            try (FileReader fileReader = new FileReader(userKeyPairPath.toFile())) {
                return KeyPairUtils.readKeyPair(fileReader);
            } catch (Throwable t) {
                throw execException(t);
            }
        }
        net.splitcells.dem.resource.Files.createDirectory(userKeyPairPath.getParent());
        try (FileWriter fw = new FileWriter(userKeyPairPath.toFile())) {
            final var newUserKeypair = KeyPairUtils.createKeyPair();
            KeyPairUtils.writeKeyPair(newUserKeypair, fw);
            return newUserKeypair;
        } catch (Throwable t) {
            throw execException(t);
        }
    }

    /**
     * TODO Persist and load generated domain key pairs.
     *
     * @return
     */
    public KeyPair domainKeyPair() {
        if (fileExists(domainKeyPairPath)) {
            try (FileReader fileReader = new FileReader(domainKeyPairPath.toFile())) {
                return KeyPairUtils.readKeyPair(fileReader);
            } catch (Throwable t) {
                throw execException(t);
            }
        }
        net.splitcells.dem.resource.Files.createDirectory(domainKeyPairPath.getParent());
        try (FileWriter fw = new FileWriter(domainKeyPairPath.toFile())) {
            final var newUserKeypair = KeyPairUtils.createKeyPair(4096);
            KeyPairUtils.writeKeyPair(newUserKeypair, fw);
            return newUserKeypair;
        } catch (Throwable t) {
            throw execException(t);
        }
    }

    public Account account(Session session, KeyPair userKeyPair) {
        try {
            var tos = session.getMetadata().getTermsOfService();

            final var accountBuilder = new AccountBuilder()
                    .agreeToTermsOfService()
                    .useKeyPair(userKeyPair);
            accountBuilder.addEmail(email);
            Account account = accountBuilder.create(session);
            logs().append(tree("Created a new account for generating public certificates via ACME.")
                            .withProperty("account location", account.getLocation().toString())
                            .withProperty("email", email)
                    , LogLevel.INFO);
            return account;
        } catch (Throwable t) {
            throw ExecutionException.execException("Could not generate account for ACME.", t);
        }
    }
}
