package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.model.Clients
import com.cjbooms.fabrikt.model.GeneratedFile
import com.cjbooms.fabrikt.model.SourceApi
import java.nio.file.Path

class OkHttpClientGenerator(
    packages: Packages,
    api: SourceApi,
    srcPath: Path,
) : ClientGenerator {
    private val simpleClientGenerator = OkHttpSimpleClientGenerator(packages, api, srcPath)
    private val enhancedClientGenerator = OkHttpEnhancedClientGenerator(packages, api, srcPath)

    override fun generate(options: Set<ClientCodeGenOptionType>): Clients {
        val simpleClient = simpleClientGenerator.generateDynamicClientCode(options)
        val enhancedClient = enhancedClientGenerator.generateDynamicClientCode(options)

        return Clients(enhancedClient.plus(simpleClient).toSet())
    }

    override fun generateLibrary(options: Set<ClientCodeGenOptionType>): Collection<GeneratedFile> {
        val simpleClientLibrary = simpleClientGenerator.generateLibrary()
        val enhancedClientLibrary = enhancedClientGenerator.generateLibrary(options)

        return simpleClientLibrary.plus(enhancedClientLibrary)
    }
}
