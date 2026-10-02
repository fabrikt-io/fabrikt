package lib

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
import io.ktor.http.Parameters
import io.ktor.http.encodeURLParameter

data class GenerationSettings(
    val genTypes: Set<CodeGenerationType>,
    val serializationLibrary: SerializationLibrary = SerializationLibrary.default,
    val instantLibrary: InstantLibrary = InstantLibrary.default,
    val jacksonNullabilityMode: JacksonNullabilityMode = JacksonNullabilityMode.default,
    val modelOptions: Set<ModelCodeGenOptionType>,
    val modelAdditionalAnnotations: List<String> = emptyList(),
    val controllerTarget: ControllerCodeGenTargetType = ControllerCodeGenTargetType.default,
    val controllerOptions: Set<ControllerCodeGenOptionType> = emptySet(),
    val modelSuffix: String = "",
    val clientOptions: Set<ClientCodeGenOptionType> = emptySet(),
    val clientTarget: ClientCodeGenTargetType = ClientCodeGenTargetType.default,
    val openfeignClientName: String = ClientCodeGenOptionType.DEFAULT_OPEN_FEIGN_CLIENT_NAME,
    /** Raw `<regex>:<replacement>` text, parsed with the CLI converter at generation time. Blank means none. */
    val operationIdTransform: String = "",
    val typeOverrides: Set<CodeGenTypeOverride> = emptySet(),
    /** Raw `type:format=Fqcn[;kotlinx=Fqcn]` entries, one per element, parsed at generation time. */
    val customTypeMappings: List<String> = emptyList(),
    val validationLibrary: ValidationLibrary = ValidationLibrary.default,
    val externalRefResolutionMode: ExternalReferencesResolutionMode = ExternalReferencesResolutionMode.default,
    val outputOptions: Set<OutputOptionType> = emptySet(),
    val inputSpec: String,
) {
    companion object {
        /**
         * Receives generation settings from the request.
         * Can be used for both query and form parameters.
         */
        fun Parameters.receiveGenerationSettings() = GenerationSettings(
            genTypes = this.getAll("genTypes")?.map { CodeGenerationType.valueOf(it) }?.toSet() ?: emptySet(),

            serializationLibrary = this["serializationLibrary"]?.let { SerializationLibrary.valueOf(it) }
                ?: SerializationLibrary.default,

            instantLibrary = this["instantLibrary"]?.let { InstantLibrary.valueOf(it) }
                ?: InstantLibrary.default,

            jacksonNullabilityMode = this["jacksonNullabilityMode"]?.let { JacksonNullabilityMode.valueOf(it) }
                ?: JacksonNullabilityMode.default,

            modelOptions = this.getAll("modelOptions")?.map { ModelCodeGenOptionType.valueOf(it) }?.toSet() ?: emptySet(),

            modelAdditionalAnnotations = this.getAll("modelAdditionalAnnotations")
                ?.flatMap { it.lines() }?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),

            controllerTarget = this["controllerTarget"]?.let { ControllerCodeGenTargetType.valueOf(it) }
                ?: ControllerCodeGenTargetType.default,

            controllerOptions = this.getAll("controllerOptions")?.map { ControllerCodeGenOptionType.valueOf(it) }
                ?.toSet() ?: emptySet(),

            modelSuffix = this["modelSuffix"] ?: "",

            clientOptions = this.getAll("clientOptions")?.map { ClientCodeGenOptionType.valueOf(it) }?.toSet()
                ?: emptySet(),

            clientTarget = this["clientTarget"]?.let { ClientCodeGenTargetType.valueOf(it) }
                ?: ClientCodeGenTargetType.default,

            openfeignClientName = this["openfeignClientName"]?.takeIf { it.isNotBlank() }
                ?: ClientCodeGenOptionType.DEFAULT_OPEN_FEIGN_CLIENT_NAME,

            operationIdTransform = this["operationIdTransform"]?.trim() ?: "",

            typeOverrides = this.getAll("typeOverrides")?.map { CodeGenTypeOverride.valueOf(it) }?.toSet()
                ?: emptySet(),

            // repeated query parameters from the URL, or one textarea value with one entry per line from the form
            customTypeMappings = this.getAll("customTypeMappings")
                ?.flatMap { it.lines() }?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),

            validationLibrary = this["validationLibrary"]?.let { ValidationLibrary.valueOf(it) }
                ?: ValidationLibrary.default,

            externalRefResolutionMode = this["externalRefResolutionMode"]?.let {
                ExternalReferencesResolutionMode.valueOf(
                    it
                )
            } ?: ExternalReferencesResolutionMode.default,

            outputOptions = this.getAll("outputOptions")?.map { OutputOptionType.valueOf(it) }?.toSet()
                ?: emptySet(),

            inputSpec = this["spec"] ?: "")
    }

    /**
     * Construct the query parameters string for the settings
     */
    fun toQueryParams(): String {
        return emptySet<String>().asSequence()
            .plus(genTypes.map { "genTypes=${it.name}" })
            .plus(serializationLibrary.let { "serializationLibrary=${it.name}" })
            .plus(instantLibrary.let { "instantLibrary=${it.name}" })
            .plus(jacksonNullabilityMode.let { "jacksonNullabilityMode=${it.name}" })
            .plus(modelOptions.map { "modelOptions=${it.name}" })
            .plus(modelAdditionalAnnotations.map { "modelAdditionalAnnotations=${it.encodeURLParameter()}" })
            .plus(controllerTarget.let { "controllerTarget=${it.name}" })
            .plus(controllerOptions.map { "controllerOptions=${it.name}" })
            .plus(modelSuffix.let { "modelSuffix=${it.encodeURLParameter()}" })
            .plus(clientOptions.map { "clientOptions=${it.name}" })
            .plus(clientTarget.let { "clientTarget=${it.name}" })
            .plus(openfeignClientName.let { "openfeignClientName=${it.encodeURLParameter()}" })
            .plus(operationIdTransform.takeIf { it.isNotBlank() }?.let { "operationIdTransform=${it.encodeURLParameter()}" })
            .plus(typeOverrides.map { "typeOverrides=${it.name}" })
            .plus(customTypeMappings.map { "customTypeMappings=${it.encodeURLParameter()}" })
            .plus(validationLibrary.let { "validationLibrary=${it.name}" })
            .plus(externalRefResolutionMode.let { "externalRefResolutionMode=${it.name}" })
            .plus(outputOptions.map { "outputOptions=${it.name}" })
            .filterNotNull()
            .joinToString("&")
    }
}
