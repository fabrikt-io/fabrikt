package com.cjbooms.fabrikt.generators.dependencies

import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.generators.dependencies.DependenciesFormatter.Notation.GRADLE
import com.cjbooms.fabrikt.generators.dependencies.DependenciesFormatter.Notation.MAVEN
import com.cjbooms.fabrikt.generators.dependencies.DependencyNotation.Companion.dependencyOf
import com.cjbooms.fabrikt.generators.dependencies.DependencyNotation.Scope.COMPILE
import com.cjbooms.fabrikt.generators.dependencies.DependencyNotation.Scope.RUNTIME
import com.cjbooms.fabrikt.util.ModelNameRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DependenciesFormatterTest {
    @BeforeEach
    fun init() {
        MutableSettings.updateSettings()
        ModelNameRegistry.clear()
    }

    @Test
    fun `should format dependencies for gradle`() {
        val expected =
            """
            api("com.fasterxml.jackson.core:jackson-core:1.2.3")
            implementation("com.fasterxml.jackson.core:jackson-databind:3.2.1")
            implementation("com.fasterxml.jackson.module:jackson-module-kotlin:1.3.5")
            """.trimIndent()
        val actual = DependenciesFormatter(GRADLE).format(someDependencies())

        assertEquals(expected, actual)
    }

    @Test
    fun `should format dependencies for maven`() {
        val expected =
            """
            <dependency>
              <groupId>com.fasterxml.jackson.core</groupId>
              <artifactId>jackson-core</artifactId>
              <version>1.2.3</version>
              <scope>runtime</scope>
            </dependency>
            <dependency>
              <groupId>com.fasterxml.jackson.core</groupId>
              <artifactId>jackson-databind</artifactId>
              <version>3.2.1</version>
              <scope>compile</scope>
            </dependency>
            <dependency>
              <groupId>com.fasterxml.jackson.module</groupId>
              <artifactId>jackson-module-kotlin</artifactId>
              <version>1.3.5</version>
              <scope>compile</scope>
            </dependency>
            """.trimIndent()
        val actual = DependenciesFormatter(MAVEN).format(someDependencies())

        assertEquals(expected, actual)
    }

    private fun someDependencies() =
        listOf(
            RUNTIME.dependencyOf("com.fasterxml.jackson.core:jackson-core:1.2.3"),
            COMPILE.dependencyOf("com.fasterxml.jackson.core:jackson-databind:3.2.1"),
            COMPILE.dependencyOf("com.fasterxml.jackson.module:jackson-module-kotlin:1.3.5"),
        )
}
