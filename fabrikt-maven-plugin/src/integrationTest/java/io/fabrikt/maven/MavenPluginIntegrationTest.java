package io.fabrikt.maven;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MavenPluginIntegrationTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void generatesAndCompilesMultipleExecutionsWithSharedConfigurationAndOverrides() throws Exception {
        Path project = copyProject("multiple-executions");

        MavenResult result = runMaven(project, "clean", "compile");

        assertThat(result.exitCode()).as(result.output()).isZero();
        Path customer = find(project.resolve("target/generated-sources"), "Customer.kt");
        Path order = find(project.resolve("target/generated-sources"), "Order.kt");
        assertThat(Files.readString(customer)).contains("jakarta.validation.constraints.NotNull");
        assertThat(Files.readString(order)).doesNotContain("validation.constraints.NotNull");
        assertThat(find(project.resolve("target/classes"), "Customer.class")).exists();
        assertThat(find(project.resolve("target/classes"), "Order.class")).exists();
    }

    @Test
    void supportsExplicitExecutionAndLeavesCleanupToMavenClean() throws Exception {
        Path project = copyProject("manual-execution");

        MavenResult firstRun = runMaven(project, "fabrikt:generate@generate-customer-api");
        assertThat(firstRun.exitCode()).as(firstRun.output()).isZero();
        Path marker = project.resolve("target/generated-sources/keep-me.txt");
        Files.writeString(marker, "marker");

        MavenResult secondRun = runMaven(project, "fabrikt:generate@generate-customer-api");
        assertThat(secondRun.exitCode()).as(secondRun.output()).isZero();
        assertThat(marker).exists();

        MavenResult cleanRun = runMaven(project, "clean", "fabrikt:generate@generate-customer-api");
        assertThat(cleanRun.exitCode()).as(cleanRun.output()).isZero();
        assertThat(marker).doesNotExist();
    }

    @Test
    void failsTheMavenBuildWhenFabriktFails() throws Exception {
        Path project = copyProject("invalid-configuration");

        MavenResult result = runMaven(project, "fabrikt:generate@generate-invalid-api");

        assertThat(result.exitCode()).isNotZero();
        assertThat(result.output()).contains("Fabrikt exited with code");
    }

    private Path copyProject(String name) throws IOException {
        Path source = Path.of("src/integrationTest/projects", name).toAbsolutePath();
        Path target = temporaryDirectory.resolve(name);
        try (var paths = Files.walk(source)) {
            paths.sorted(Comparator.naturalOrder()).forEach(path -> copy(path, source, target));
        }

        Path pom = target.resolve("pom.xml");
        String configuredPom = Files.readString(pom)
                .replace("@FABRIKT_VERSION@", System.getProperty("fabrikt.plugin.version"))
                .replace("@TEST_REPOSITORY@", System.getProperty("fabrikt.test.repository"));
        Files.writeString(pom, configuredPom);
        return target;
    }

    private void copy(Path source, Path sourceRoot, Path targetRoot) {
        Path target = targetRoot.resolve(sourceRoot.relativize(source).toString());
        try {
            if (Files.isDirectory(source)) {
                Files.createDirectories(target);
            } else {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not copy Maven integration test project", exception);
        }
    }

    private MavenResult runMaven(Path project, String... goals) throws Exception {
        Path mavenHome = Path.of(System.getProperty("fabrikt.maven.home"));
        String executableName = System.getProperty("os.name").toLowerCase().contains("windows") ? "mvn.cmd" : "mvn";
        List<String> command = new ArrayList<>();
        command.add(mavenHome.resolve("bin").resolve(executableName).toString());
        command.add("--batch-mode");
        command.add("--no-transfer-progress");
        command.add("-Dmaven.repo.local=" + temporaryDirectory.resolve("maven-repository"));
        command.addAll(List.of(goals));

        Process process = new ProcessBuilder(command)
                .directory(project.toFile())
                .redirectErrorStream(true)
                .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return new MavenResult(process.waitFor(), output);
    }

    private Path find(Path root, String fileName) throws IOException {
        try (var paths = Files.walk(root)) {
            return paths.filter(path -> path.getFileName().toString().equals(fileName))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(fileName + " was not found below " + root));
        }
    }

    private record MavenResult(int exitCode, String output) {}
}
