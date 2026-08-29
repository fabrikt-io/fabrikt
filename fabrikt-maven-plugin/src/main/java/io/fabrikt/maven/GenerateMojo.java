package io.fabrikt.maven;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/** Generates Kotlin sources from one OpenAPI specification using Fabrikt. */
@Mojo(name = "generate", threadSafe = true)
public final class GenerateMojo extends AbstractMojo {
    private final CliArgumentMapper argumentMapper;
    private final FabriktJarLocator jarLocator;
    private final FabriktProcessRunner processRunner;

    /** OpenAPI specification processed by this execution. */
    @Parameter(required = true)
    private String inputFile;

    /** Directory below which Fabrikt writes its normal source tree. */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources", required = true)
    private File outputDirectory;

    /** Fabrikt CLI arguments expressed as camelCase XML elements. */
    @Parameter(required = true)
    private PlexusConfiguration arguments;

    @Parameter(defaultValue = "${project.basedir}", readonly = true, required = true)
    private File projectDirectory;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${plugin}", readonly = true, required = true)
    private PluginDescriptor pluginDescriptor;

    /** Creates the mojo instance used by Maven's plugin container. */
    public GenerateMojo() {
        this(new CliArgumentMapper(), new FabriktJarLocator(), new JavaProcessRunner());
    }

    GenerateMojo(
            CliArgumentMapper argumentMapper,
            FabriktJarLocator jarLocator,
            FabriktProcessRunner processRunner) {
        this.argumentMapper = argumentMapper;
        this.jarLocator = jarLocator;
        this.processRunner = processRunner;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Path projectPath = projectDirectory.toPath();
        Path outputPath = outputDirectory.toPath();
        List<String> cliArguments = argumentMapper.map(projectPath, inputFile, outputPath, arguments);
        Path fabriktJar = jarLocator.locate(pluginDescriptor);

        int exitCode;
        try {
            exitCode = processRunner.run(fabriktJar, cliArguments, projectPath, getLog());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new MojoExecutionException("Fabrikt execution was interrupted", exception);
        } catch (IOException exception) {
            throw new MojoExecutionException("Could not start Fabrikt", exception);
        }

        if (exitCode != 0) {
            throw new MojoFailureException("Fabrikt exited with code " + exitCode);
        }
        registerGeneratedSources(outputPath);
    }

    private void registerGeneratedSources(Path outputPath) {
        Path mainSources = outputPath.resolve("src/main/kotlin").toAbsolutePath().normalize();
        Path testSources = outputPath.resolve("src/test/kotlin").toAbsolutePath().normalize();
        if (Files.isDirectory(mainSources)) {
            project.addCompileSourceRoot(mainSources.toString());
        }
        if (Files.isDirectory(testSources)) {
            project.addTestCompileSourceRoot(testSources.toString());
        }
    }

    void configureForTest(
            String inputFile,
            File outputDirectory,
            PlexusConfiguration arguments,
            File projectDirectory,
            MavenProject project,
            PluginDescriptor pluginDescriptor) {
        this.inputFile = inputFile;
        this.outputDirectory = outputDirectory;
        this.arguments = arguments;
        this.projectDirectory = projectDirectory;
        this.project = project;
        this.pluginDescriptor = pluginDescriptor;
    }
}
