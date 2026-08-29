package io.fabrikt.maven;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FabriktJarLocatorTest {
    private final FabriktJarLocator locator = new FabriktJarLocator();

    @TempDir
    Path temporaryDirectory;

    @Test
    void locatesFabriktArtifactWithPluginVersion() throws Exception {
        Path fabriktJar = temporaryDirectory.resolve("fabrikt-1.2.3.jar");
        fabriktJar.toFile().createNewFile();
        PluginDescriptor descriptor = descriptor("1.2.3", artifact("io.fabrikt", "fabrikt", "1.2.3", fabriktJar));

        assertThat(locator.locate(descriptor)).isEqualTo(fabriktJar);
    }

    @Test
    void rejectsFabriktArtifactWithDifferentVersion() throws Exception {
        Path fabriktJar = temporaryDirectory.resolve("fabrikt-1.2.2.jar");
        fabriktJar.toFile().createNewFile();
        PluginDescriptor descriptor = descriptor("1.2.3", artifact("io.fabrikt", "fabrikt", "1.2.2", fabriktJar));

        assertThatThrownBy(() -> locator.locate(descriptor))
                .isInstanceOf(MojoExecutionException.class)
                .hasMessage("Could not locate the Fabrikt executable JAR with plugin version 1.2.3");
    }

    private PluginDescriptor descriptor(String version, Artifact artifact) {
        PluginDescriptor descriptor = new PluginDescriptor();
        descriptor.setVersion(version);
        descriptor.setArtifacts(List.of(artifact));
        return descriptor;
    }

    private Artifact artifact(String groupId, String artifactId, String version, Path file) {
        return (Artifact) Proxy.newProxyInstance(
                Artifact.class.getClassLoader(),
                new Class<?>[] {Artifact.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getGroupId" -> groupId;
                    case "getArtifactId" -> artifactId;
                    case "getVersion" -> version;
                    case "getFile" -> file.toFile();
                    case "toString" -> groupId + ":" + artifactId + ":" + version;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == arguments[0];
                    default -> defaultValue(method.getReturnType());
                });
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        return 0;
    }
}
