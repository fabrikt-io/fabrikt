package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ClientBearerSecurityTest {
    private val document = SourceApi(readTextResource("/examples/bearerSecurity/api.yaml")).openApi3
    private val resolver = ClientBearerSecurity(document)

    @Test
    fun `global Bearer requirement is inherited`() {
        assertThat(plan("/protected")).isEqualTo(BearerSecurityPlan(listOf("BearerAuth"), tokenRequired = true))
    }

    @Test
    fun `empty operation security overrides global requirement`() {
        assertThat(plan("/anonymous")).isNull()
    }

    @Test
    fun `anonymous alternative makes Bearer optional`() {
        assertThat(plan("/optional")).isEqualTo(BearerSecurityPlan(listOf("BearerAuth"), tokenRequired = false))
    }

    @Test
    fun `Bearer combined with unsupported scheme is not treated as satisfied`() {
        assertThat(plan("/combined")).isNull()
    }

    @Test
    fun `another security alternative makes Bearer optional`() {
        assertThat(plan("/alternative")).isEqualTo(BearerSecurityPlan(listOf("BearerAuth"), tokenRequired = false))
    }

    private fun plan(path: String): BearerSecurityPlan? =
        resolver.forOperation(
            document.paths
                .getValue(path)
                .operations
                .getValue("get"),
        )
}
