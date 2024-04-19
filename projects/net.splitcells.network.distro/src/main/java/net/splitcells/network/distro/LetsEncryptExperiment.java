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
import net.splitcells.dem.data.set.Set;
import net.splitcells.dem.resource.Paths;
import net.splitcells.dem.resource.communication.log.LogLevel;
import net.splitcells.dem.resource.communication.log.Logs;
import net.splitcells.dem.utils.StringUtils;
import net.splitcells.website.Formats;
import net.splitcells.website.server.Config;
import net.splitcells.website.server.processor.BinaryMessage;
import net.splitcells.website.server.projects.ProjectsRendererI;
import net.splitcells.website.server.projects.extension.ProjectsRendererExtension;
import net.splitcells.website.server.projects.extension.ProjectsRendererExtensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.util.KeyPairUtils;

import java.io.FileReader;
import java.io.FileWriter;
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
import static net.splitcells.dem.utils.ExecutionException.executionException;
import static net.splitcells.dem.utils.NotImplementedYet.notImplementedYet;
import static net.splitcells.network.distro.AcmeChallengeFile.acmeChallengeFile;

public class LetsEncryptExperiment {
    public static void main(String... args) {
        Dem.process(() -> {
            Distro.service().start();
            sleepAtLeast(3000l);
            System.out.println(new LetsEncryptExperiment("contacts@splitcells.net").certificate("live.splitcells.net"));
        }, env -> {
            env.config()
                    .withInitedOption(CurrentAcmeAuthorization.class)
                    .configValue(ProjectsRendererExtensions.class)
                    .withAppended(acmeChallengeFile());
        });
    }

    public static void certificate(String domain, String email) {
        System.out.println("Retrieved certificate: " + new LetsEncryptExperiment("contacts@splitcells.net").certificate("live.splitcells.net"));
    }

    private final String sessionUrl = "acme://letsencrypt.org";
    private final String email;
    private final Path userKeyPairPath = Paths.userHome("acme-user-key-pair");
    private final Path domainKeyPairPath = Paths.userHome("acme-domain-key-pair");

    private LetsEncryptExperiment(String emailArg) {
        Security.addProvider(new BouncyCastleProvider());
        email = emailArg;
    }

    public Certificate certificate(String domain) {
        try {
            final var userKeyPair = userKeyPair();
            final var domainKeyPair = domainKeyPair();
            final var session = new Session(sessionUrl);
            final var account = account(session, userKeyPair);
            return certificate(domain, account, domainKeyPair);
        } catch (Throwable t) {
            throw executionException(t);
        }
    }

    private Certificate certificate(String domain, Account account, KeyPair domainKeyPair) {
        try {
            final var order = account.newOrder().domain(domain).create();
            order.getAuthorizations().forEach(this::authorize);
            order.execute(domainKeyPair);
            for (int i = 0; i < 10; ++i) {
                if (Status.INVALID.equals(order.getStatus())) {
                    throw executionException("Creating the certificate failed.");
                }
                if (Status.VALID.equals(order.getStatus())) {
                    return order.getCertificate();
                }
                final var now = Instant.now();
                final var updateTime = order.fetch().orElseGet(() -> Instant.now().plusSeconds(3L));
                sleepAtLeast(now.until(updateTime, ChronoUnit.MILLIS));
            }
        } catch (Throwable t) {
            throw executionException(t);
        }
        throw executionException("Could not fetch certificate.");
    }

    private void authorize(Authorization auth) {
        try {
            if (Status.VALID.equals(auth.getStatus())) {
                return;
            }
            configValue(CurrentAcmeAuthorization.class).withValue(Optional.of(auth));
            final var challenge = auth.findChallenge(Http01Challenge.class).orElseThrow();
            System.out.println(challenge.getToken());
            for (int i = 0; i < 50; ++i) {
                if (Status.INVALID.equals(challenge.getStatus())) {
                    throw executionException("Could not complete ACME challenge.");
                }
                if (Status.VALID.equals(challenge.getStatus())) {
                    return;
                }
                final var now = Instant.now();
                final var updateTime = challenge.fetch().orElseGet(() -> Instant.now().plusSeconds(3L));
                sleepAtLeast(now.until(updateTime, ChronoUnit.MILLIS));
            }
        } catch (Throwable t) {
            throw executionException(t);
        }
        throw executionException("Could not fetch certificate.");
    }

    public KeyPair userKeyPair() {
        if (Files.exists(userKeyPairPath)) {
            try (FileReader fileReader = new FileReader(userKeyPairPath.toFile())) {
                return KeyPairUtils.readKeyPair(fileReader);
            } catch (Throwable t) {
                throw executionException(t);
            }
        }
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
            Logs.logs().append(perspective("Created a new account for generating public certificates via ACME.")
                            .withProperty("account location", account.getLocation().toString())
                            .withProperty("email", email)
                    , LogLevel.INFO);
            return account;
        } catch (Throwable t) {
            throw executionException("Could not generate account for ACME.", t);
        }
    }
}
