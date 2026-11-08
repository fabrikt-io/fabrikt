package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.ApiSecurityGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.Linter
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApiSecurityGeneratorTest {
    private val spec = readTextResource("/examples/apiSecurity/api.yaml")
    private val packages = Packages("examples.apiSecurity")

    @BeforeEach
    fun reset() {
        MutableSettings.updateSettings()
        ModelNameRegistry.clear()
    }

    @Test
    fun `emits one target-neutral security declaration`() {
        assertThatGenerated(generate()).isEqualTo("/examples/apiSecurity/client/ApiSecurity.kt")
    }

    @Test
    fun `requirements are keyed by the raw operation id`() {
        val generated = generate()

        assertThat(generated).contains("\"get_document_by_id\" to")
        assertThat(generated).doesNotContain("\"getDocumentById\" to")
    }

    @Test
    fun `operations without an operation id are keyed by verb and path`() {
        val generated = generate()

        assertThat(generated).contains("\"GET /anonymous\" to")
        assertThat(generated).contains("\"GET /documents/{id}\" to")
    }

    private fun generate(): String = Linter.lintString(ApiSecurityGenerator(packages, SourceApi(spec)).generate().toString())
}
