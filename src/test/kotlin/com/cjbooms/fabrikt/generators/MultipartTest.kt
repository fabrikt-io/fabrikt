package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OkHttpSimpleClientGenerator
import com.cjbooms.fabrikt.generators.model.JacksonMetadata
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.ClientType
import com.cjbooms.fabrikt.model.Models
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.Linter
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.squareup.kotlinpoet.FileSpec
import java.nio.file.Paths
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MultipartTest {

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            clientTarget = ClientCodeGenTargetType.OK_HTTP,
        )
    }

    @Test
    fun `multipart form data generates correct client and model code`() {
        val packages = Packages("examples.multipartUpload")
        val apiLocation = this::class.java.getResource("/examples/multipartUpload/api.yaml")!!
        val api = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))

        val expectedModel = readTextResource("/examples/multipartUpload/models/ClientModels.kt")
        val expectedClient = readTextResource("/examples/multipartUpload/client/ApiClient.kt")

        val models = ModelGenerator(packages, api).generate().toSingleFile()
        val simpleClientCode = OkHttpSimpleClientGenerator(packages, api)
            .generateDynamicClientCode()
            .toSingleFile()

        assertThat(models).isEqualTo(expectedModel)
        assertThat(simpleClientCode).isEqualTo(expectedClient)
    }

    private fun Collection<ClientType>.toSingleFile(): String {
        val destPackage = if (this.isNotEmpty()) first().destinationPackage else ""
        val singleFileBuilder = FileSpec.builder(destPackage, "dummyFilename")
        this.forEach {
            val builder = singleFileBuilder
                .addType(it.spec)
                .addImport(JacksonMetadata.TYPE_REFERENCE_IMPORT.first, JacksonMetadata.TYPE_REFERENCE_IMPORT.second)
            builder.build()
        }
        return Linter.lintString(singleFileBuilder.build().toString())
    }

    private fun Models.toSingleFile(): String {
        val destPackage = if (models.isNotEmpty()) models.first().destinationPackage else ""
        val singleFileBuilder = FileSpec.builder(destPackage, "dummyFilename")
        models
            .sortedBy { it.spec.name }
            .forEach {
                val builder = singleFileBuilder
                    .addType(it.spec)
                builder.build()
            }
        return Linter.lintString(singleFileBuilder.build().toString())
    }
}