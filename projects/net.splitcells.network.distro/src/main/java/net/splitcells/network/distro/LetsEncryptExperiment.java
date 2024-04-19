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

import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;

import java.security.KeyPair;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static net.splitcells.dem.utils.ExecutionException.executionException;
import static net.splitcells.dem.utils.NotImplementedYet.notImplementedYet;

public class LetsEncryptExperiment {
    public static void main(String... args) {
        System.out.println(new LetsEncryptExperiment().certificate("live.splitcells.net"));
    }

    private LetsEncryptExperiment() {
        throw notImplementedYet();
    }

    public Certificate certificate(String domain) {
        try {
            final var userKeyPair = userKeyPair();
            final var domainKeyPair = domainKeyPair();
            final var session = new Session("acme://letsencrypt.org");
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
                    throw executionException("Creating the certificate failed");
                }
                if (Status.VALID.equals(order.getStatus())) {
                    return order.getCertificate();
                }
                final var now = Instant.now();
                final var updateTime = order.fetch().orElseGet(() -> Instant.now().plusSeconds(3L));
                Thread.sleep(now.until(updateTime, ChronoUnit.MILLIS));
            }
        } catch (Throwable t) {
            throw executionException(t);
        }
        throw notImplementedYet();
    }

    private void authorize(Authorization auth) {
        throw notImplementedYet();
    }

    public KeyPair userKeyPair() {
        throw notImplementedYet();
    }

    public KeyPair domainKeyPair() {
        throw notImplementedYet();
    }

    public Account account(Session session, KeyPair userKeyPair) {
        throw notImplementedYet();
    }
}
