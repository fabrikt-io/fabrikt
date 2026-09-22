package com.cjbooms.fabrikt.generators.dependencies

/**
 * Use this to indicate that the source code generated based on particular option/setting/generator
 * (any class relevant for configuration of code generation) may require dependencies to compile/run times
 */
fun interface MaybeRequiringDependencies {
    fun requiredDependencies(): List<DependencyNotation>
}
