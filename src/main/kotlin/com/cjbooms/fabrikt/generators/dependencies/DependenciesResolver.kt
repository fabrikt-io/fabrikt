package com.cjbooms.fabrikt.generators.dependencies

import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.generators.MutableSettings

class DependenciesResolver(
    private val settings: MutableSettings,
) {
    fun resolve(): List<DependencyNotation> =
        buildList {
            addAll(resolveModelsDependencies())
            addAll(resolveClientDependencies())
            addAll(resolveControllerDependencies())
        }.distinct()

    private fun resolveControllerDependencies(): List<DependencyNotation> =
        takeIf { settings.generationTypes.contains(CodeGenerationType.CONTROLLERS) }
            ?.let {
                buildList {
                    add(settings.controllerTarget)
                    addAll(settings.controllerOptions)
                }
            }?.flatMap { it.requiredDependencies() }
            ?: emptyList()

    private fun resolveClientDependencies(): List<DependencyNotation> =
        takeIf { settings.generationTypes.contains(CodeGenerationType.CLIENT) }
            ?.let {
                buildList {
                    add(settings.clientTarget)
                    addAll(settings.clientOptions)
                }
            }?.flatMap { it.requiredDependencies() }
            ?: emptyList()

    private fun resolveModelsDependencies(): List<DependencyNotation> =
        buildList {
            addAll(settings.modelOptions)
            add(settings.validationLibrary)
            add(settings.serializationLibrary)
            addAll(settings.typeOverrides)
            if (CodeGenTypeOverride.DATETIME_AS_INSTANT in settings.typeOverrides) {
                add(settings.instantLibrary)
            }
        }.flatMap { it.requiredDependencies() }
}
