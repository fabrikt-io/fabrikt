package io.fabrikt.maven;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;

final class FabriktJarLocator {
    private static final String FABRIKT_GROUP_ID = "io.fabrikt";
    private static final String FABRIKT_ARTIFACT_ID = "fabrikt";

    Path locate(PluginDescriptor pluginDescriptor) throws MojoExecutionException {
        return pluginDescriptor.getArtifacts().stream()
                .filter(this::isFabriktArtifact)
                .filter(artifact -> pluginDescriptor.getVersion().equals(artifact.getVersion()))
                .map(Artifact::getFile)
                .filter(file -> file != null && Files.isRegularFile(file.toPath()))
                .map(File::toPath)
                .findFirst()
                .orElseThrow(() -> new MojoExecutionException(
                        "Could not locate the Fabrikt executable JAR with plugin version "
                                + pluginDescriptor.getVersion()));
    }

    private boolean isFabriktArtifact(Artifact artifact) {
        return FABRIKT_GROUP_ID.equals(artifact.getGroupId())
                && FABRIKT_ARTIFACT_ID.equals(artifact.getArtifactId());
    }
}
