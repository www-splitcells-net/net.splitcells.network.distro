/* SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro.java.acme;

import net.splitcells.dem.data.set.Set;
import net.splitcells.dem.data.set.Sets;
import net.splitcells.dem.lang.annotations.JavaLegacy;
import net.splitcells.dem.utils.StringUtils;
import net.splitcells.website.Format;
import net.splitcells.website.server.Config;
import net.splitcells.website.server.processor.BinaryMessage;
import net.splitcells.website.server.projects.ProjectsRendererI;
import net.splitcells.website.server.projects.RenderRequest;
import net.splitcells.website.server.projects.extension.ProjectsRendererExtension;
import org.shredzone.acme4j.challenge.Http01Challenge;

import java.nio.file.Path;
import java.util.Optional;

import static net.splitcells.dem.Dem.configValue;
import static net.splitcells.dem.data.set.Sets.setOfUniques;

@JavaLegacy
public class AcmeChallengeFile implements ProjectsRendererExtension {
    public static AcmeChallengeFile acmeChallengeFile() {
        return new AcmeChallengeFile();
    }

    private final String challengeResponsePath = "/.well-known/acme-challenge/";

    private AcmeChallengeFile() {

    }

    @Override
    public Optional<BinaryMessage> renderFile(String path, ProjectsRendererI projectsRenderer, Config config) {
        final var currentAcmeChallenge = configValue(CurrentAcmeAuthorization.class);
        if (currentAcmeChallenge.value().isEmpty()) {
            return Optional.empty();
        }
        final var currentPath = challengeResponsePath
                + currentAcmeChallenge.value().get()
                .findChallenge(Http01Challenge.class)
                .orElseThrow()
                .getToken();
        if (currentPath.equals(path)) {
            return Optional.of(BinaryMessage.binaryMessage(
                    StringUtils.toBytes(configValue(CurrentAcmeAuthorization.class)
                            .value()
                            .orElseThrow()
                            .findChallenge(Http01Challenge.class)
                            .orElseThrow()
                            .getAuthorization())
                    , Format.TEXT_PLAIN));
        }
        return Optional.empty();
    }

    @Override
    public boolean requiresAuthentication(RenderRequest request) {
        return false;
    }

    @Override
    public Set<Path> projectPaths(ProjectsRendererI projectsRenderer) {
        final var currentAcmeChallenge = configValue(CurrentAcmeAuthorization.class);
        if (currentAcmeChallenge.value().isPresent()) {
            return Sets.setOfUniques(Path.of(challengeResponsePath.substring(1)
                    + currentAcmeChallenge.value().get()
                    .findChallenge(Http01Challenge.class)
                    .orElseThrow()
                    .getToken()));
        }
        return setOfUniques();
    }
}
