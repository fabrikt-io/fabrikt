package io.fabrikt.maven;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.configuration.PlexusConfiguration;

final class CliArgumentMapper {
    private static final Pattern CAMEL_CASE_BOUNDARY = Pattern.compile("([a-z0-9])([A-Z])");
    private static final Pattern REMOTE_INPUT = Pattern.compile("(?i)^https?://.*");
    private static final Set<String> RESERVED_ARGUMENTS = Set.of("api-file", "input-file", "output-directory");

    List<String> map(
            Path projectDirectory,
            String inputFile,
            Path outputDirectory,
            PlexusConfiguration configuredArguments)
            throws MojoFailureException {
        if (inputFile == null || inputFile.isBlank()) {
            throw new MojoFailureException("inputFile must be configured for every Fabrikt execution");
        }
        if (!hasValue(configuredArguments, "base-package")) {
            throw new MojoFailureException("arguments.basePackage must be configured for every Fabrikt execution");
        }

        List<String> result = new ArrayList<>();
        result.add("--output-directory");
        result.add(outputDirectory.toAbsolutePath().normalize().toString());
        result.add("--api-file");
        result.add(resolveInput(projectDirectory, inputFile));

        for (PlexusConfiguration argument : configuredArguments.getChildren()) {
            String argumentName = toKebabCase(argument.getName());
            if (RESERVED_ARGUMENTS.contains(argumentName)) {
                throw new MojoFailureException(
                        "arguments." + argument.getName() + " is managed by the Maven plugin and cannot be overridden");
            }
            String option = "--" + argumentName;
            for (String value : values(argument)) {
                if ("true".equalsIgnoreCase(value)) {
                    result.add(option);
                } else if (!"false".equalsIgnoreCase(value)) {
                    result.add(option);
                    result.add(value);
                }
            }
        }
        return List.copyOf(result);
    }

    private String resolveInput(Path projectDirectory, String inputFile) {
        if (REMOTE_INPUT.matcher(inputFile).matches()) {
            return inputFile;
        }
        return projectDirectory.resolve(inputFile).toAbsolutePath().normalize().toString();
    }

    private boolean hasValue(PlexusConfiguration configuration, String argumentName) {
        if (configuration == null) {
            return false;
        }
        for (PlexusConfiguration child : configuration.getChildren()) {
            if (argumentName.equals(toKebabCase(child.getName()))
                    && values(child).stream().anyMatch(value -> !value.isBlank())) {
                return true;
            }
        }
        return false;
    }

    private List<String> values(PlexusConfiguration configuration) {
        if (configuration == null) {
            return List.of();
        }
        if (configuration.getChildCount() == 0) {
            String value = configuration.getValue(null);
            return value == null ? List.of() : List.of(value);
        }

        List<String> values = new ArrayList<>();
        for (PlexusConfiguration child : configuration.getChildren()) {
            values.addAll(values(child));
        }
        return values;
    }

    private String toKebabCase(String name) {
        return CAMEL_CASE_BOUNDARY.matcher(name).replaceAll("$1-$2").toLowerCase(Locale.ROOT);
    }
}
