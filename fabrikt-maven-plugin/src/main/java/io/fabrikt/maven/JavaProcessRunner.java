package io.fabrikt.maven;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.logging.Log;

final class JavaProcessRunner implements FabriktProcessRunner {
    @Override
    public int run(Path executableJar, List<String> arguments, Path workingDirectory, Log log)
            throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(javaExecutable().toString());
        command.add("-jar");
        command.add(executableJar.toString());
        command.addAll(arguments);

        Process process = new ProcessBuilder(command)
                .directory(workingDirectory.toFile())
                .redirectErrorStream(true)
                .start();

        try (BufferedReader output = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            output.lines().forEach(log::info);
        }
        return process.waitFor();
    }

    private Path javaExecutable() {
        Path binDirectory = Path.of(System.getProperty("java.home"), "bin");
        Path windowsExecutable = binDirectory.resolve("java.exe");
        return Files.isRegularFile(windowsExecutable) ? windowsExecutable : binDirectory.resolve("java");
    }
}
