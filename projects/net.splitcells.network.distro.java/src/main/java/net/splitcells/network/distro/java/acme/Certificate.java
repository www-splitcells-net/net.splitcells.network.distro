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

import net.splitcells.dem.Dem;
import net.splitcells.dem.environment.config.ProgramName;
import net.splitcells.dem.resource.Paths;
import net.splitcells.dem.resource.communication.log.LogLevel;
import net.splitcells.network.distro.java.Distro;
import net.splitcells.website.server.config.PublicContactEMailAddress;
import net.splitcells.website.server.config.PublicDomain;
import net.splitcells.website.server.projects.extension.ProjectsRendererExtensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.util.KeyPairUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.Security;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static net.splitcells.dem.Dem.configValue;
import static net.splitcells.dem.Dem.sleepAtLeast;
import static net.splitcells.dem.data.set.Sets.setOfUniques;
import static net.splitcells.dem.lang.perspective.PerspectiveI.perspective;
import static net.splitcells.dem.resource.communication.log.Logs.logs;
import static net.splitcells.dem.utils.ExecutionException.executionException;
import static net.splitcells.dem.utils.NotImplementedYet.notImplementedYet;
import static net.splitcells.network.distro.java.acme.AcmeChallengeFile.acmeChallengeFile;

/**
 * One can use `https://letsdebug.net/` in order to debug `https://letsencrypt.org/`.
 * Published certificates can be found via `https://crt.sh/`.
 */
public class Certificate {
    public static void main(String... args) {
        Dem.process(() -> {
            Distro.service().start();
            sleepAtLeast(3000l);
            System.out.println(new Certificate("contacts@splitcells.net").certificatePem("live.splitcells.net"));
        }, env -> {
            env.config()
                    .withInitedOption(CurrentAcmeAuthorization.class)
                    .configValue(ProjectsRendererExtensions.class)
                    .withAppended(acmeChallengeFile());
        });
    }

    public static byte[] certificatePem() {
        return certificatePem(configValue(PublicDomain.class).orElseThrow()
                , configValue(PublicContactEMailAddress.class).orElseThrow());
    }

    private static byte[] certificatePem(String domain, String email) {
        final var certificateStream = new ByteArrayOutputStream();
        final var certificateWriter = new OutputStreamWriter(certificateStream);
        try {
            new Certificate(email)
                    .certificatePem(domain)
                    .writeCertificate(certificateWriter);
            certificateWriter.flush();
        } catch (IOException e) {
            throw executionException(e);
        }
        return certificateStream.toByteArray();
    }

    private static final long TIME_BETWEEN_CHECKS = 3l;
    private final String sessionUrl = configValue(AcmeServerUri.class);
    private final String email;
    // TODO Create portable file storage concept.
    private final Path userKeyPairPath = Paths.userHome(".local", "state", configValue(ProgramName.class), "acme-user-key-pair");
    // TODO Create portable file storage concept.
    private final Path domainKeyPairPath = Paths.userHome(".local", "state", configValue(ProgramName.class), "acme-domain-key-pair");
    private final Path acmeCertificatePath = Paths.userHome(".local", "state", configValue(ProgramName.class), "acme-certificate.pem");

    private Certificate(String emailArg) {
        Security.addProvider(new BouncyCastleProvider());
        email = emailArg;
    }

    public org.shredzone.acme4j.Certificate certificatePem(String domain) {
        try {
            final var userKeyPair = userKeyPair();
            final var domainKeyPair = domainKeyPair();
            final var session = new Session(sessionUrl);
            final var account = account(session, userKeyPair);
            final var certificate = certificatePem(domain, account, domainKeyPair);
            try (FileWriter fw = new FileWriter(acmeCertificatePath.toFile())) {
                certificate.writeCertificate(fw);
            }
            return certificate;
        } catch (Throwable t) {
            throw executionException(t);
        }
    }

    private org.shredzone.acme4j.Certificate certificatePem(String domain, Account account, KeyPair domainKeyPair) {
        try {
            final var order = account.newOrder().domain(domain).create();
            order.getAuthorizations().forEach(this::authorize);
            order.execute(domainKeyPair);
            // From experience this can take a lot of time. So any time limit, does not make any sense for now.
            int i = 0;
            while (true) {
                ++i;
                logs().append(perspective("Waiting for `" + sessionUrl + "` to provide certificate.")
                                .withProperty("status", order.getStatus().toString())
                                .withProperty("error", order.getError().map(e -> e.toString()).orElse("No error is present."))
                                .withProperty("status check count", "" + i)
                        , LogLevel.INFO);
                if (Status.INVALID.equals(order.getStatus())) {
                    throw executionException("Creating the certificate failed.");
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
            throw executionException(t);
        }
    }

    private void authorize(Authorization auth) {
        try {
            if (Status.VALID.equals(auth.getStatus())) {
                return;
            }
            configValue(CurrentAcmeAuthorization.class).withValue(Optional.of(auth));
            final var challenge = auth.findChallenge(Http01Challenge.class).orElseThrow();
            logs().append(perspective("Waiting for `" + sessionUrl + "` to execute the challenge.")
                            .withProperty("token", challenge.getToken())
                            .withProperty("identifier", auth.getIdentifier().getValue())
                            .withProperty("type of identifier", auth.getIdentifier().getType())
                    , LogLevel.INFO);
            challenge.trigger();
            // From experience this can take a lot of time. So any time limit, does not make any sense for now.
            int i = 0;
            while (true) {
                ++i;
                logs().append(perspective("Waiting for `" + sessionUrl + "` to execute the challenge.")
                                .withProperty("status", challenge.getStatus().toString())
                                .withProperty("error", challenge.getError().map(e -> e.toString())
                                        .orElse("No error is present."))
                                .withProperty("status check count", "" + i)
                        , LogLevel.INFO);
                if (Status.INVALID.equals(challenge.getStatus())) {
                    throw executionException("Could not complete ACME challenge.");
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
            throw executionException(t);
        }
    }

    public KeyPair userKeyPair() {
        if (Files.exists(userKeyPairPath)) {
            try (FileReader fileReader = new FileReader(userKeyPairPath.toFile())) {
                return KeyPairUtils.readKeyPair(fileReader);
            } catch (Throwable t) {
                throw executionException(t);
            }
        }
        net.splitcells.dem.resource.Files.createDirectory(userKeyPairPath.getParent());
        try (FileWriter fw = new FileWriter(userKeyPairPath.toFile())) {
            final var newUserKeypair = KeyPairUtils.createKeyPair();
            KeyPairUtils.writeKeyPair(newUserKeypair, fw);
            return newUserKeypair;
        } catch (Throwable t) {
            throw executionException(t);
        }
    }

    /**
     * TODO Persist and load generated domain key pairs.
     *
     * @return
     */
    public KeyPair domainKeyPair() {
        if (Files.exists(domainKeyPairPath)) {
            try (FileReader fileReader = new FileReader(domainKeyPairPath.toFile())) {
                return KeyPairUtils.readKeyPair(fileReader);
            } catch (Throwable t) {
                throw executionException(t);
            }
        }
        net.splitcells.dem.resource.Files.createDirectory(domainKeyPairPath.getParent());
        try (FileWriter fw = new FileWriter(domainKeyPairPath.toFile())) {
            final var newUserKeypair = KeyPairUtils.createKeyPair(4096);
            KeyPairUtils.writeKeyPair(newUserKeypair, fw);
            return newUserKeypair;
        } catch (Throwable t) {
            throw executionException(t);
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
            logs().append(perspective("Created a new account for generating public certificates via ACME.")
                            .withProperty("account location", account.getLocation().toString())
                            .withProperty("email", email)
                    , LogLevel.INFO);
            return account;
        } catch (Throwable t) {
            throw executionException("Could not generate account for ACME.", t);
        }
    }
}
