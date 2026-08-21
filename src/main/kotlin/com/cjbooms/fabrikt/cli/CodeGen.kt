package com.cjbooms.fabrikt.cli

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ApiFileLoader
import com.cjbooms.fabrikt.util.AuthJsonLoader
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

        val resolvedAuth = ApiFileLoader.resolveHeaders(codeGenArgs.auth, System::getenv)
        val jsonLoader = if (resolvedAuth.isNotEmpty()) AuthJsonLoader(resolvedAuth) else null

        generate(
            basePackage = codeGenArgs.basePackage,
            apiFile = codeGenArgs.apiFile,
            outputDir = codeGenArgs.outputDirectory,
            apiFragments = codeGenArgs.apiFragments,
            srcPath = codeGenArgs.srcPath,
            resourcesPath = codeGenArgs.resourcesPath,
            resolvedAuth = resolvedAuth,
            jsonLoader = jsonLoader,
        )
    }

    private fun generate(
        basePackage: String,
        apiFile: String,
        outputDir: Path,
        apiFragments: List<String> = emptyList(),
        srcPath: Path,
        resourcesPath: Path,
        resolvedAuth: List<Pair<String, String>> = emptyList(),
        jsonLoader: com.reprezen.jsonoverlay.JsonLoader? = null,
    ) {
        val suppliedApi = ApiFileLoader.load(apiFile, "--api-file", resolvedAuth)
        val fragments = apiFragments.map { ApiFileLoader.load(it, "--api-fragment", resolvedAuth).content }

        logger.info("Generating code and dumping to $outputDir/")

        val packages = Packages(basePackage)
        val sourceApi = SourceApi.create(suppliedApi.content, fragments, suppliedApi.baseUri, jsonLoader)
        val generator = CodeGenerator(packages, sourceApi, srcPath, resourcesPath)
        generator.generate().forEach { it.writeFileTo(outputDir.toFile()) }
    }
}
