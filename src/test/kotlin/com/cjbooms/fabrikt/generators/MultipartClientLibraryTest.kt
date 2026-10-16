package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OpenFeignInterfaceGenerator
import com.cjbooms.fabrikt.generators.client.SpringHttpInterfaceGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MultipartClientLibraryTest {
    private val packages = Packages("examples.multipartClientParts")

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(genTypes = setOf(CodeGenerationType.CLIENT))
        ModelNameRegistry.clear()
    }

    @Test
    fun `multipart runtime helpers are only emitted for multipart specifications`() {
        val multipart = SourceApi(readTextResource("/examples/multipartClientParts/api.yaml"))
        val ordinary = SourceApi(readTextResource("/examples/openFeignClient/api.yaml"))

        assertThat(OpenFeignInterfaceGenerator(packages, multipart).generateLibrary(emptySet())).hasSize(1)
        assertThat(SpringHttpInterfaceGenerator(packages, multipart).generateLibrary(emptySet())).hasSize(1)
        assertThat(OpenFeignInterfaceGenerator(packages, ordinary).generateLibrary(emptySet())).isEmpty()
        assertThat(SpringHttpInterfaceGenerator(packages, ordinary).generateLibrary(emptySet())).isEmpty()
    }
}
