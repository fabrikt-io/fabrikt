package io.fabrikt.maven;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.maven.plugin.logging.Log;

interface FabriktProcessRunner {
    int run(Path executableJar, List<String> arguments, Path workingDirectory, Log log)
            throws IOException, InterruptedException;
}
