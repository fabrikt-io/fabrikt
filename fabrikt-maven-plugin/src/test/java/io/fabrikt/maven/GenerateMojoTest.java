package io.fabrikt.maven;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.DefaultPlexusConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GenerateMojoTest {
    @TempDir
    Path projectDirectory;

    @Test
    void executesFabriktAndRegistersExistingSourceRoots() throws Exception {
        RecordingRunner runner = new RecordingRunner(0, true);
        GenerateMojo mojo = configuredMojo(runner);

        mojo.execute();

        Path outputDirectory = projectDirectory.resolve("target/generated-sources");
        assertThat(runner.arguments)
                .containsExactly(
                        "--output-directory",
                        outputDirectory.toString(),
                        "--api-file",
                        projectDirectory.resolve("customer.yaml").toString(),
                        "--base-package",
                        "com.example.customer");
        assertThat(runner.workingDirectory).isEqualTo(projectDirectory);
        assertThat(runner.executableJar).isEqualTo(projectDirectory.resolve("fabrikt.jar"));
        assertThat(runner.project.getCompileSourceRoots())
                .contains(outputDirectory.resolve("src/main/kotlin").toString());
        assertThat(runner.project.getTestCompileSourceRoots())
                .contains(outputDirectory.resolve("src/test/kotlin").toString());
    }

    @Test
    void propagatesNonZeroFabriktExitCode() throws Exception {
        GenerateMojo mojo = configuredMojo(new RecordingRunner(7, false));

        assertThatThrownBy(mojo::execute)
                .isInstanceOf(MojoFailureException.class)
                .hasMessage("Fabrikt exited with code 7");
    }

    private GenerateMojo configuredMojo(RecordingRunner runner) throws Exception {
        Path fabriktJar = projectDirectory.resolve("fabrikt.jar");
        Files.createFile(fabriktJar);
        PluginDescriptor descriptor = new PluginDescriptor();
        descriptor.setVersion("1.0.0");
        descriptor.setArtifacts(List.of(artifact(fabriktJar)));

        DefaultPlexusConfiguration arguments = new DefaultPlexusConfiguration("arguments");
        DefaultPlexusConfiguration basePackage = new DefaultPlexusConfiguration("basePackage");
        basePackage.setValue("com.example.customer");
        arguments.addChild(basePackage);

        MavenProject project = new MavenProject();
        runner.project = project;
        GenerateMojo mojo = new GenerateMojo(new CliArgumentMapper(), new FabriktJarLocator(), runner);
        mojo.configureForTest(
                "customer.yaml",
                projectDirectory.resolve("target/generated-sources").toFile(),
                arguments,
                projectDirectory.toFile(),
                project,
                descriptor);
        return mojo;
    }

    private Artifact artifact(Path file) {
        return (Artifact) Proxy.newProxyInstance(
                Artifact.class.getClassLoader(),
                new Class<?>[] {Artifact.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getGroupId" -> "io.fabrikt";
                    case "getArtifactId" -> "fabrikt";
                    case "getVersion" -> "1.0.0";
                    case "getFile" -> file.toFile();
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == arguments[0];
                    default -> null;
                });
    }

    private static final class RecordingRunner implements FabriktProcessRunner {
        private final int exitCode;
        private final boolean createSources;
        private Path executableJar;
        private List<String> arguments;
        private Path workingDirectory;
        private MavenProject project;

        private RecordingRunner(int exitCode, boolean createSources) {
            this.exitCode = exitCode;
            this.createSources = createSources;
        }

        @Override
        public int run(
                Path executableJar,
                List<String> arguments,
                Path workingDirectory,
                org.apache.maven.plugin.logging.Log log)
                throws java.io.IOException {
            this.executableJar = executableJar;
            this.arguments = arguments;
            this.workingDirectory = workingDirectory;
            if (createSources) {
                Path outputDirectory = Path.of(arguments.get(1));
                Files.createDirectories(outputDirectory.resolve("src/main/kotlin"));
                Files.createDirectories(outputDirectory.resolve("src/test/kotlin"));
            }
            return exitCode;
        }
    }
}
