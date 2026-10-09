package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.CustomTypeMapping
import com.cjbooms.fabrikt.cli.ExternalReferencesResolutionMode
import com.cjbooms.fabrikt.cli.InstantLibrary
import com.cjbooms.fabrikt.cli.JacksonNullabilityMode
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.OutputOptionType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.ValidationLibrary
import com.cjbooms.fabrikt.model.GenerationMetadata
import com.cjbooms.fabrikt.model.MicronautSerdeAnnotations
import com.cjbooms.fabrikt.model.SerializationAnnotations
import java.util.logging.Logger

object MutableSettings {
    private val logger = Logger.getGlobal()
    var generationMetadata: GenerationMetadata = GenerationMetadata()
        private set
    var generationTypes: Set<CodeGenerationType> = mutableSetOf()
        private set
    var controllerOptions: Set<ControllerCodeGenOptionType> = mutableSetOf()
        private set
    var controllerTarget: ControllerCodeGenTargetType = ControllerCodeGenTargetType.default
        private set
    var modelOptions: Set<ModelCodeGenOptionType> = mutableSetOf()
        private set
    var modelAdditionalAnnotations: List<String> = emptyList()
        private set
    var modelSuffix: String = ""
        private set
    var clientOptions: Set<ClientCodeGenOptionType> = mutableSetOf()
        private set
    var clientTarget: ClientCodeGenTargetType = ClientCodeGenTargetType.default
        private set
    var openfeignClientName: String = ClientCodeGenOptionType.DEFAULT_OPEN_FEIGN_CLIENT_NAME
        private set
    var typeOverrides: Set<CodeGenTypeOverride> = mutableSetOf()
        private set
    var customTypeMappings: List<CustomTypeMapping> = emptyList()
        private set
    var validationLibrary: ValidationLibrary = ValidationLibrary.default
        private set
    var externalRefResolutionMode: ExternalReferencesResolutionMode = ExternalReferencesResolutionMode.default
        private set
    var serializationLibrary: SerializationLibrary = SerializationLibrary.default
        private set
    var instantLibrary: InstantLibrary = InstantLibrary.default
        private set
    var jacksonNullabilityMode: JacksonNullabilityMode = JacksonNullabilityMode.default
        private set
    var outputOptions: Set<OutputOptionType> = mutableSetOf()
        private set
    var operationIdTransform: Pair<Regex, String>? = null
        private set

    /**
     * Returns the effective serialization annotations to use.
     * If MICRONAUT_SERDEABLE option is enabled, uses MicronautSerdeAnnotations.
     * Otherwise, uses the serialization annotations from the configured serialization library.
     */
    val effectiveSerializationAnnotations: SerializationAnnotations
        get() =
            if (ModelCodeGenOptionType.MICRONAUT_SERDEABLE in modelOptions) {
                MicronautSerdeAnnotations
            } else {
                serializationLibrary.serializationAnnotations
            }

    /**
     * Returns the effective nullability mode for Jackson serialization. If Jackson
     * is used, returns [jacksonNullabilityMode], otherwise returns [JacksonNullabilityMode.NONE].
     */
    val effectiveJacksonNullabilityMode: JacksonNullabilityMode
        get() = if (serializationLibrary.isJackson) jacksonNullabilityMode else JacksonNullabilityMode.NONE

    fun updateSettings(
        genTypes: Set<CodeGenerationType> = emptySet(),
        controllerOptions: Set<ControllerCodeGenOptionType> = emptySet(),
        controllerTarget: ControllerCodeGenTargetType = ControllerCodeGenTargetType.default,
        modelOptions: Set<ModelCodeGenOptionType> = emptySet(),
        modelAdditionalAnnotations: List<String> = emptyList(),
        modelSuffix: String = "",
        clientOptions: Set<ClientCodeGenOptionType> = emptySet(),
        clientTarget: ClientCodeGenTargetType = ClientCodeGenTargetType.default,
        openfeignClientName: String = ClientCodeGenOptionType.DEFAULT_OPEN_FEIGN_CLIENT_NAME,
        typeOverrides: Set<CodeGenTypeOverride> = emptySet(),
        customTypeMappings: List<CustomTypeMapping> = emptyList(),
        validationLibrary: ValidationLibrary = ValidationLibrary.default,
        externalRefResolutionMode: ExternalReferencesResolutionMode = ExternalReferencesResolutionMode.default,
        serializationLibrary: SerializationLibrary = SerializationLibrary.default,
        instantLibrary: InstantLibrary = InstantLibrary.default,
        jacksonNullabilityMode: JacksonNullabilityMode = JacksonNullabilityMode.default,
        outputOptions: Set<OutputOptionType> = emptySet(),
        operationIdTransform: Pair<Regex, String>? = null,
        generationMetadata: GenerationMetadata = GenerationMetadata(),
    ) {
        this.generationMetadata = generationMetadata
        this.generationTypes = genTypes
        this.controllerOptions = controllerOptions
        this.controllerTarget = controllerTarget
        this.modelOptions = modelOptions
        this.modelAdditionalAnnotations = modelAdditionalAnnotations
        this.modelSuffix = modelSuffix
        this.clientOptions = clientOptions
        this.clientTarget = clientTarget
        this.openfeignClientName = openfeignClientName
        this.typeOverrides = typeOverrides
        warnOnConflictingTypeOverrides()
        this.customTypeMappings = customTypeMappings
        this.validationLibrary = validationLibrary
        this.externalRefResolutionMode = externalRefResolutionMode
        this.serializationLibrary = serializationLibrary
        this.instantLibrary = instantLibrary
        this.jacksonNullabilityMode = jacksonNullabilityMode
        this.outputOptions = outputOptions
        this.operationIdTransform = operationIdTransform
    }

    fun addOption(option: ModelCodeGenOptionType) {
        modelOptions += option
    }

    fun addOption(override: CodeGenTypeOverride) {
        typeOverrides += override
        warnOnConflictingTypeOverrides()
    }

    fun addOption(mode: JacksonNullabilityMode) {
        jacksonNullabilityMode = mode
    }

    /**
     * Groups the type overrides that resolve for the same OAS type. Options in a group are mutually
     * exclusive, so selecting two logs a warning and the fixed precedence in `KotlinTypeInfo.from`
     * decides the result. Different OAS types use different groups (e.g. `date` and `date-time` are
     * separate) so combining overrides across types never warns. `BYTEARRAY_AS_INPUTSTREAM` sits with
     * the binary overrides because it only affects the ByteArray producer, which `string/byte` does
     * not use.
     */
    private fun warnOnConflictingTypeOverrides() {
        val families =
            listOf(
                setOf(
                    CodeGenTypeOverride.DATETIME_AS_STRING,
                    CodeGenTypeOverride.DATETIME_AS_INSTANT,
                    CodeGenTypeOverride.DATETIME_AS_LOCALDATETIME,
                    CodeGenTypeOverride.DATETIME_AS_OFFSETDATETIME,
                ),
                setOf(
                    CodeGenTypeOverride.BINARY_AS_STRING,
                    CodeGenTypeOverride.BINARY_AS_BYTEARRAY,
                    CodeGenTypeOverride.BYTEARRAY_AS_INPUTSTREAM,
                ),
                setOf(
                    CodeGenTypeOverride.URI_AS_STRING,
                    CodeGenTypeOverride.URI_AS_URI,
                ),
                setOf(
                    CodeGenTypeOverride.BYTE_AS_STRING,
                    CodeGenTypeOverride.BYTE_AS_BYTEARRAY,
                ),
                setOf(
                    CodeGenTypeOverride.UUID_AS_STRING,
                    CodeGenTypeOverride.UUID_AS_UUID,
                ),
                setOf(
                    CodeGenTypeOverride.DATE_AS_STRING,
                    CodeGenTypeOverride.DATE_AS_LOCALDATE,
                    CodeGenTypeOverride.DATE_AS_KOTLINXLOCALDATE,
                ),
                setOf(
                    CodeGenTypeOverride.ANY_AS_JSONELEMENT,
                    CodeGenTypeOverride.ANY_AS_ANY,
                ),
            )
        families.forEach { family ->
            val present = family.filter { it in typeOverrides }.map { it.name }.sorted()
            if (present.size > 1) {
                logger.warning("Conflicting --type-overrides: ${present.joinToString(", ")}; using fixed precedence.")
            }
        }
    }
}
