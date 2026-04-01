package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.SHOULD_OVERWRITE_EXAMPLES
import com.cjbooms.fabrikt.util.ResourceHelper.getFileNamesAndPathsInFolder
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.nio.file.Files.deleteIfExists
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.io.path.Path as KPath

class GeneratedCodeAsserter(val generatedCode: String, val expectedFiles: Path? = null) {
    val expectedModelNames: Map<String, Path>? = expectedFiles?.let { getFileNamesAndPathsInFolder(it) }

    companion object {
        // Set this to true to overwrite the expected files with the generated code when they don't match
        const val SHOULD_OVERWRITE_EXAMPLES = false

        fun assertThatGenerated(generatedCode: String): GeneratedCodeAsserter = GeneratedCodeAsserter(generatedCode)

        fun assertThatExpectedFiles(expectedFiles: Path): GeneratedCodeAsserter = GeneratedCodeAsserter("", expectedFiles)

        fun failGenerated(generatedCode: String) = GeneratedCodeAsserter(generatedCode)

        fun maybeGenerateMissingFile(resourcePath: String, generatedCode: String) {
            if (SHOULD_OVERWRITE_EXAMPLES) {
                println("Mismatch found. Attempting to fix the source file.")
                val sourceFilePath: Path = KPath("src", "test", "resources", resourcePath)
                println("Overwriting existing, or creating absent, file: $sourceFilePath")
                sourceFilePath.parent?.let { java.nio.file.Files.createDirectories(it) }
                sourceFilePath.writeText(generatedCode)
            }
        }

        fun maybeDeleteExpectedFile(expectedFile: Path) {
            if (SHOULD_OVERWRITE_EXAMPLES) {
                println("Expected file ${expectedFile.fileName} not found in generated files. Attempting to delete the expected file.")
                deleteIfExists(expectedFile)
            }
        }
    }

    /**
     * Asserts that the generated code is equal to the content of the resource file at the given path.
     * @param resourcePath The path to the resource file to compare against.
     */
    fun isEqualTo(resourcePath: String) {
        try {
            val expectedText = readTextResource(resourcePath)
            assertThat(generatedCode).isEqualTo(expectedText)
        } catch (ex: Throwable) {
            maybeGenerateMissingFile(resourcePath, generatedCode)
            if (!SHOULD_OVERWRITE_EXAMPLES) {
                throw ex
            }
        }
    }

    fun asFileNotFound(expectedPath: String, failureMessage: String) {
        try {
            fail(failureMessage + expectedPath)
        } catch (ex: AssertionError) {
            maybeGenerateMissingFile(expectedPath, generatedCode)
            if (!SHOULD_OVERWRITE_EXAMPLES) {
                throw ex
            }
        }
    }

    fun areContainedInGenerated(generatedFiles: Map<String, String>) {
        expectedModelNames?.forEach {
            try {
                assertThat(generatedFiles.contains(it.key))
                    .withFailMessage { "Expected model file $it not found in generated models" }
                    .isTrue()
            } catch (ex: AssertionError) {
                maybeDeleteExpectedFile(it.value)
                if (!SHOULD_OVERWRITE_EXAMPLES) {
                    throw ex
                }
            }
        }
    }
}

class OverWriteProtectionTest {
    @Test
    fun `should fail if the overwrite files is set to true to prevent accidental commit`() {
        assertThat(SHOULD_OVERWRITE_EXAMPLES).isFalse()
    }
}

