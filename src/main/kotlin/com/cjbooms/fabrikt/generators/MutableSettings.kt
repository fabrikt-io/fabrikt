package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.ExternalReferencesResolutionMode
import com.cjbooms.fabrikt.cli.InstantLibrary
import com.cjbooms.fabrikt.cli.JacksonNullabilityMode
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.OutputOptionType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.ValidationLibrary
import com.cjbooms.fabrikt.model.MicronautSerdeAnnotations
import com.cjbooms.fabrikt.model.SerializationAnnotations

object MutableSettings {
    var generationTypes: Set<CodeGenerationType> = mutableSetOf()
        private set
    var controllerOptions: Set<ControllerCodeGenOptionType> = mutableSetOf()
        private set
    var controllerTarget: ControllerCodeGenTargetType = ControllerCodeGenTargetType.default
        private set
    var modelOptions: Set<ModelCodeGenOptionType> = mutableSetOf()
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

    /**
     * Returns the effective serialization annotations to use.
     * If MICRONAUT_SERDEABLE option is enabled, uses MicronautSerdeAnnotations.
     * Otherwise, uses the serialization annotations from the configured serialization library.
     */
    val effectiveSerializationAnnotations: SerializationAnnotations
        get() = if (ModelCodeGenOptionType.MICRONAUT_SERDEABLE in modelOptions) {
            MicronautSerdeAnnotations
        } else {
            serializationLibrary.serializationAnnotations
        }

    /**
     * Returns the effective nullability mode for Jackson serialization. If Jackson
     * is used, returns [jacksonNullabilityMode], otherwise returns [JacksonNullabilityMode.NONE].
     */
    val effectiveJacksonNullabilityMode: JacksonNullabilityMode
        get() = if (serializationLibrary == SerializationLibrary.JACKSON) jacksonNullabilityMode else JacksonNullabilityMode.NONE


    fun updateSettings(
        genTypes: Set<CodeGenerationType> = emptySet(),
        controllerOptions: Set<ControllerCodeGenOptionType> = emptySet(),
        controllerTarget: ControllerCodeGenTargetType = ControllerCodeGenTargetType.default,
        modelOptions: Set<ModelCodeGenOptionType> = emptySet(),
        modelSuffix: String = "",
        clientOptions: Set<ClientCodeGenOptionType> = emptySet(),
        clientTarget: ClientCodeGenTargetType = ClientCodeGenTargetType.default,
        openfeignClientName: String = ClientCodeGenOptionType.DEFAULT_OPEN_FEIGN_CLIENT_NAME,
        typeOverrides: Set<CodeGenTypeOverride> = emptySet(),
        validationLibrary: ValidationLibrary = ValidationLibrary.default,
        externalRefResolutionMode: ExternalReferencesResolutionMode = ExternalReferencesResolutionMode.default,
        serializationLibrary: SerializationLibrary = SerializationLibrary.default,
        instantLibrary: InstantLibrary = InstantLibrary.default,
        jacksonNullabilityMode: JacksonNullabilityMode = JacksonNullabilityMode.default,
        outputOptions: Set<OutputOptionType> = emptySet()
    ) {
        this.generationTypes = genTypes
        this.controllerOptions = controllerOptions
        this.controllerTarget = controllerTarget
        this.modelOptions = modelOptions - ModelCodeGenOptionType.SEALED_INTERFACES_FOR_ONE_OF
        this.modelSuffix = modelSuffix
        this.clientOptions = clientOptions
        this.clientTarget = clientTarget
        this.openfeignClientName = openfeignClientName
        this.typeOverrides = typeOverrides
        this.validationLibrary = validationLibrary
        this.externalRefResolutionMode = externalRefResolutionMode
        this.serializationLibrary = serializationLibrary
        this.instantLibrary = instantLibrary
        this.jacksonNullabilityMode = jacksonNullabilityMode
        this.outputOptions = outputOptions
    }

    fun addOption(option: ModelCodeGenOptionType) {
        modelOptions += option
    }

    fun addOption(override: CodeGenTypeOverride) {
        typeOverrides += override
    }

    fun addOption(mode: JacksonNullabilityMode) {
        jacksonNullabilityMode = mode
    }

    fun isSealedInterfacesForOneOfEnabled(): Boolean =
        ModelCodeGenOptionType.DISABLE_SEALED_INTERFACES_FOR_ONE_OF !in modelOptions
}
