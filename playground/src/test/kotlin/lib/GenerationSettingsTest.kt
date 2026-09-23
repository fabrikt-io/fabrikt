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
import lib.GenerationSettings.Companion.receiveGenerationSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GenerationSettingsTest {

    @Test
    fun generatedAnnotationOutputOption() {
        val settings = Parameters.build {
            append("outputOptions", "ADD_GENERATED_ANNOTATION")
        }.receiveGenerationSettings()

        assertEquals(setOf(OutputOptionType.ADD_GENERATED_ANNOTATION), settings.outputOptions)
        assertTrue(settings.toQueryParams().contains("outputOptions=ADD_GENERATED_ANNOTATION"))
    }

    @Test
    fun modelAdditionalAnnotationsFromForm() {
        val settings = Parameters.build {
            append("modelAdditionalAnnotations", "com.example.First\ncom.example.Second")
        }.receiveGenerationSettings()

        assertEquals(listOf("com.example.First", "com.example.Second"), settings.modelAdditionalAnnotations)
        assertTrue(settings.toQueryParams().contains("modelAdditionalAnnotations=com.example.First"))
        assertTrue(settings.toQueryParams().contains("modelAdditionalAnnotations=com.example.Second"))
    }

    @Test
    fun toQueryParams() {
        val settings = GenerationSettings(
            genTypes = setOf(CodeGenerationType.HTTP_MODELS),
            serializationLibrary = SerializationLibrary.KOTLINX_SERIALIZATION,
            instantLibrary = InstantLibrary.KOTLIN_TIME_INSTANT,
            jacksonNullabilityMode = JacksonNullabilityMode.STRICT,
            modelOptions = setOf(ModelCodeGenOptionType.SEALED_INTERFACES_FOR_ONE_OF),
            modelAdditionalAnnotations = listOf("com.example.First", "com.example.Second"),
            controllerTarget = ControllerCodeGenTargetType.KTOR,
            controllerOptions = setOf(ControllerCodeGenOptionType.AUTHENTICATION),
            modelSuffix = "Model",
            clientOptions = setOf(ClientCodeGenOptionType.RESILIENCE4J),
            clientTarget = ClientCodeGenTargetType.OK_HTTP,
            openfeignClientName = "my-client",
            operationIdTransform = "^V2_(.*):v2$1",
            typeOverrides = setOf(CodeGenTypeOverride.DATETIME_AS_INSTANT),
            customTypeMappings = listOf("string:uuid=java.util.UUID", "string:money=com.example.Money"),
            validationLibrary = ValidationLibrary.JAVAX_VALIDATION,
            externalRefResolutionMode = ExternalReferencesResolutionMode.TARGETED,
            outputOptions = setOf(OutputOptionType.ADD_FILE_DISCLAIMER),
            inputSpec = "spec"
        )

        val queryParams = settings.toQueryParams()

        assertEquals("""
            genTypes=HTTP_MODELS
            &serializationLibrary=KOTLINX_SERIALIZATION
            &instantLibrary=KOTLIN_TIME_INSTANT
            &jacksonNullabilityMode=STRICT
            &modelOptions=SEALED_INTERFACES_FOR_ONE_OF
            &modelAdditionalAnnotations=com.example.First
            &modelAdditionalAnnotations=com.example.Second
            &controllerTarget=KTOR
            &controllerOptions=AUTHENTICATION
            &modelSuffix=Model
            &clientOptions=RESILIENCE4J
            &clientTarget=OK_HTTP
            &openfeignClientName=my-client
            &operationIdTransform=%5EV2_%28.%2A%29%3Av2%241
            &typeOverrides=DATETIME_AS_INSTANT
            &customTypeMappings=string%3Auuid%3Djava.util.UUID
            &customTypeMappings=string%3Amoney%3Dcom.example.Money
            &validationLibrary=JAVAX_VALIDATION
            &externalRefResolutionMode=TARGETED
            &outputOptions=ADD_FILE_DISCLAIMER
        """.trimIndent().replace("\n",""), queryParams)
    }
}
