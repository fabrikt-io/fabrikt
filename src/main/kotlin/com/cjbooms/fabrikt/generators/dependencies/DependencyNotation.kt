package com.cjbooms.fabrikt.generators.dependencies

data class DependencyNotation(
    val scope: Scope,
    val groupId: String,
    val artifactId: String,
    val version: String,
) {
    companion object {
        fun Scope.dependencyOf(gradleNotation: String) =
            gradleNotation
                .split(":")
                .let { (groupId, artifactId, version) ->
                    DependencyNotation(
                        scope = this,
                        groupId = groupId,
                        artifactId = artifactId,
                        version = version,
                    )
                }
    }

    /**
     * Scope as defined in: https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#dependency-scope
     * Limited to used scopes in the project.
     *
     * [COMPILE] - Indicates that dependency will be present in compile classpath of this and consumer's project
     * (consumers don't have to use declare the dependency to access during compilation)
     * [RUNTIME] - Indicates that dependency will be present in compile classpath of generated project,
     * however only on runtime of consumer's project
     */
    enum class Scope {
        COMPILE,
        RUNTIME,
    }
}
