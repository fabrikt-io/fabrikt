package com.cjbooms.fabrikt.cli

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ApiFileLoader
import java.nio.file.Path
import java.util.logging.Logger

object CodeGen {

    private val logger = Logger.getGlobal()

    @JvmStatic
    fun main(args: Array<String>) {
        val codeGenArgs = CodeGenArgs.parse(args)

        MutableSettings.updateSettings(
            genTypes = codeGenArgs.targets,
            controllerOptions = codeGenArgs.controllerOptions,
            controllerTarget = codeGenArgs.controllerTarget,
            modelOptions = codeGenArgs.modelOptions,
            modelSuffix = codeGenArgs.modelSuffix,
            clientOptions = codeGenArgs.clientOptions,
            clientTarget = codeGenArgs.clientTarget,
            openfeignClientName = codeGenArgs.openfeignClientName,
            typeOverrides = codeGenArgs.typeOverrides,
            validationLibrary = codeGenArgs.validationLibrary,
            externalRefResolutionMode = codeGenArgs.externalRefResolutionMode,
            serializationLibrary = codeGenArgs.serializationLibrary,
            instantLibrary = codeGenArgs.instantLibrary,
            jacksonNullabilityMode = codeGenArgs.jacksonNullabilityMode,
            outputOptions = codeGenArgs.outputOptions,
        )
        generate(
            basePackage = codeGenArgs.basePackage,
            apiFile = codeGenArgs.apiFile,
            outputDir = codeGenArgs.outputDirectory,
            apiFragments = codeGenArgs.apiFragments,
            srcPath = codeGenArgs.srcPath,
            resourcesPath = codeGenArgs.resourcesPath,
        )
    }

    private fun generate(
        basePackage: String,
        apiFile: String,
        outputDir: Path,
        apiFragments: List<String> = emptyList(),
        srcPath: Path,
        resourcesPath: Path,
    ) {
        val suppliedApi = ApiFileLoader.load(apiFile, "--api-file")
        val fragments = apiFragments.map { ApiFileLoader.load(it, "--api-fragment").content }

        logger.info("Generating code and dumping to $outputDir/")

        val packages = Packages(basePackage)
        val sourceApi = SourceApi.create(suppliedApi.content, fragments, suppliedApi.baseUri)
        val generator = CodeGenerator(packages, sourceApi, srcPath, resourcesPath)
        generator.generate().forEach { it.writeFileTo(outputDir.toFile()) }
    }
}
