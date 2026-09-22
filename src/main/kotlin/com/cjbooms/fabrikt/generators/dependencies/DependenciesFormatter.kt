package com.cjbooms.fabrikt.generators.dependencies

import com.cjbooms.fabrikt.generators.dependencies.DependenciesFormatter.Notation.GRADLE
import com.cjbooms.fabrikt.generators.dependencies.DependenciesFormatter.Notation.MAVEN
import com.cjbooms.fabrikt.generators.dependencies.DependencyNotation.Scope.COMPILE
import com.cjbooms.fabrikt.generators.dependencies.DependencyNotation.Scope.RUNTIME

class DependenciesFormatter(
    private val notation: Notation,
) {
    enum class Notation {
        GRADLE,
        MAVEN,
    }

    fun format(dependencies: List<DependencyNotation>): String =
        when (notation) {
            GRADLE -> dependencies.joinToString(separator = "\n") { it.format(notation) }
            MAVEN -> dependencies.joinToString(separator = "\n") { it.format(notation) }
        }

    private fun DependencyNotation.format(notation: Notation): String =
        when (notation) {
            GRADLE -> "${scope.asGradleScope()}(\"$groupId:$artifactId:$version\")"
            MAVEN ->
                """
                <dependency>
                  <groupId>$groupId</groupId>
                  <artifactId>$artifactId</artifactId>
                  <version>$version</version>
                  <scope>${scope.asMavenScope()}</scope>
                </dependency>
                """.trimIndent()
        }

    private fun DependencyNotation.Scope.asGradleScope() =
        when (this) {
            COMPILE -> "implementation"
            RUNTIME -> "api"
        }

    private fun DependencyNotation.Scope.asMavenScope() =
        when (this) {
            COMPILE -> "compile"
            RUNTIME -> "runtime"
        }
}
