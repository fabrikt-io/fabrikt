package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.model.Clients
import com.cjbooms.fabrikt.model.GeneratedFile

interface ClientGenerator {
    fun generate(options: Set<ClientCodeGenOptionType>): Clients

    fun generateLibrary(options: Set<ClientCodeGenOptionType>): Collection<GeneratedFile>
}
