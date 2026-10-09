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

    override fun generate(options: Set<ClientCodeGenOptionType>): Clients =
        Clients(simpleClientGenerator.generateDynamicClientCode(options))

    override fun generateLibrary(options: Set<ClientCodeGenOptionType>): Collection<GeneratedFile> =
        simpleClientGenerator.generateLibrary(options)
}
