package com.cjbooms.fabrikt.util

import com.beust.jcommander.ParameterException
import java.io.IOException
import java.util.concurrent.TimeUnit

object AuthHeaderResolver {
    private val SHELL_TIMEOUT_SECONDS = 10L
    private val ENV_VAR_NAME_REGEX = Regex("^[A-Za-z_][A-Za-z0-9_]*$")
    private val ENV_VAR_PLACEHOLDER_REGEX = Regex("\\$\\{([A-Za-z_][A-Za-z0-9_]*)(?::-([^}]*))?\\}")
    private val SHELL_COMMAND_REGEX = Regex("(^|\\s)!(\\S.*)")

    fun resolveHeaders(
        headers: List<String>,
        env: (String) -> String? = System::getenv,
    ): List<Pair<String, String>> = headers.map { resolveHeader(it, env) }

    private fun resolveHeader(
        header: String,
        env: (String) -> String?,
    ): Pair<String, String> {
        val colonIndex = header.indexOf(':')
        if (colonIndex == -1) {
            throw ParameterException("Invalid --auth '$header': expected 'Name: value'")
        }
        val name = header.substring(0, colonIndex).trim()
        val rawValue = header.substring(colonIndex + 1)
        return name to resolveHeaderValue(rawValue, env)
    }

    fun resolveHeaderValue(
        value: String,
        env: (String) -> String?,
    ): String {
        val trimmed = value.trim()

        SHELL_COMMAND_REGEX.find(trimmed)?.let { match ->
            val prefix = trimmed.substring(0, match.range.first) + match.groupValues[1]
            val command = match.groupValues[2]
            val result = runCommand(command)
            if (result.isBlank()) {
                throw ParameterException("Auth command '$command' produced no output")
            }
            return prefix + result
        }

        if (ENV_VAR_NAME_REGEX.matches(trimmed)) {
            env(trimmed)?.takeIf { it.isNotEmpty() }?.let { return it }
        }

        return ENV_VAR_PLACEHOLDER_REGEX.replace(trimmed) { match ->
            val varName = match.groupValues[1]
            val default = match.groupValues[2]
            env(varName)?.takeIf { it.isNotEmpty() } ?: default
        }
    }

    private fun runCommand(command: String): String {
        val process =
            try {
                ProcessBuilder("sh", "-c", command).start()
            } catch (e: IOException) {
                throw ParameterException("Failed to run auth command '$command'", e)
            }
        if (!process.waitFor(SHELL_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            throw ParameterException("Auth command '$command' timed out after ${SHELL_TIMEOUT_SECONDS}s")
        }
        if (process.exitValue() != 0) {
            val stderr =
                process.errorStream
                    .bufferedReader()
                    .use { it.readText() }
                    .trim()
            throw ParameterException(
                "Auth command '$command' failed with exit code ${process.exitValue()}" +
                    if (stderr.isNotEmpty()) ": $stderr" else "",
            )
        }
        return process.inputStream
            .bufferedReader()
            .use { it.readText() }
            .trim()
    }
}
