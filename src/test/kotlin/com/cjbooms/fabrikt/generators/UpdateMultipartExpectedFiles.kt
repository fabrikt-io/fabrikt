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
import com.squareup.kotlinpoet.FileSpec
import java.io.File
import java.nio.file.Paths
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateMultipartExpectedFiles {

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            clientTarget = ClientCodeGenTargetType.OK_HTTP,
        )
    }

    @Test
    fun `update expected multipart code files`() {
        val packages = Packages("examples.multipartUpload")
        val apiLocation = this::class.java.getResource("/examples/multipartUpload/api.yaml")!!
        val api = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))

        val models = ModelGenerator(packages, api).generate().toSingleFile()
        val simpleClientCode = OkHttpSimpleClientGenerator(packages, api)
            .generateDynamicClientCode()
            .toSingleFile()

        // Overwrite existing expected files
        File("src/test/resources/examples/multipartUpload/models/ClientModels.kt").writeText(models)
        File("src/test/resources/examples/multipartUpload/client/ApiClient.kt").writeText(simpleClientCode)

        println("Updated expected files:")
        println("- src/test/resources/examples/multipartUpload/models/ClientModels.kt")
        println("- src/test/resources/examples/multipartUpload/client/ApiClient.kt")

        println("\nGenerated client code:")
        println(simpleClientCode)
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