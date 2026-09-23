package lib

import com.cjbooms.fabrikt.cli.CodeGenerator
import com.cjbooms.fabrikt.cli.CustomTypeMapping
import com.cjbooms.fabrikt.cli.OperationIdTransform
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.model.Destinations
import com.cjbooms.fabrikt.model.SourceApi

fun generateCodeSynchronized(
    generationSettings: GenerationSettings
) = synchronized(MutableSettings) { // necessary because of the mutable static state
    MutableSettings.updateSettings(
        genTypes = generationSettings.genTypes,
        controllerOptions = generationSettings.controllerOptions,
        controllerTarget = generationSettings.controllerTarget,
        modelOptions = generationSettings.modelOptions,
        modelAdditionalAnnotations = generationSettings.modelAdditionalAnnotations,
        modelSuffix = generationSettings.modelSuffix,
        clientOptions = generationSettings.clientOptions,
        clientTarget = generationSettings.clientTarget,
        openfeignClientName = generationSettings.openfeignClientName,
        typeOverrides = generationSettings.typeOverrides,
        customTypeMappings = generationSettings.parseCustomTypeMappings(),
        validationLibrary = generationSettings.validationLibrary,
        externalRefResolutionMode = generationSettings.externalRefResolutionMode,
        serializationLibrary = generationSettings.serializationLibrary,
        instantLibrary = generationSettings.instantLibrary,
        jacksonNullabilityMode = generationSettings.jacksonNullabilityMode,
        outputOptions = generationSettings.outputOptions,
        operationIdTransform = generationSettings.parseOperationIdTransform(),
    )

    val packages = Packages("com.example")
    val sourceApi = SourceApi.create(generationSettings.inputSpec, emptyList())
    val generator = CodeGenerator(packages, sourceApi, Destinations.MAIN_KT_SOURCE, Destinations.MAIN_RESOURCES)

    generator.generate().distinct()
}

/** Same parser as `--operation-id-transform`, so the playground accepts exactly the CLI syntax. */
private fun GenerationSettings.parseOperationIdTransform(): Pair<Regex, String>? =
    operationIdTransform.takeIf { it.isNotBlank() }?.let { OperationIdTransform.parse(it) }

/** Same parser as `--custom-type-mapping`, so the playground accepts exactly the CLI syntax. */
private fun GenerationSettings.parseCustomTypeMappings(): List<CustomTypeMapping> =
    customTypeMappings.map { CustomTypeMapping.parse(it) }
