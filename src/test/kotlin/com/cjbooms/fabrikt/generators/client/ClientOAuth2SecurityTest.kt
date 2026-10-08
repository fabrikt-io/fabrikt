package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ClientOAuth2SecurityTest {
    @Test
    fun `device authorization flow uses the same operation scope contract`() {
        val spec =
            readTextResource("/examples/oauth2Security/api.yaml")
                .replace("3.0.4", "3.2.0")
                .replace("clientCredentials:", "deviceAuthorization:\n          deviceAuthorizationUrl: https://example.com/device")
        val document = SourceApi(spec).openApi3
        val operation =
            document.paths
                .getValue("/protected")
                .operations
                .getValue("get")
        assertThat(ClientOAuth2Security(document).forOperation(operation)).isEqualTo(
            OAuth2SecurityPlan(listOf(OAuth2SecurityAlternative("OAuth2", listOf("read:pets"))), tokenRequired = true),
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.0.4", "3.1.2", "3.2.0"])
    fun `effective security preserves scope alternatives and overrides`(version: String) {
        val document = SourceApi(readTextResource("/examples/oauth2Security/api.yaml").replace("3.0.4", version)).openApi3
        val resolver = ClientOAuth2Security(document)

        fun plan(path: String) =
            resolver.forOperation(
                document.paths
                    .getValue(path)
                    .operations
                    .getValue("get"),
            )
        val read = OAuth2SecurityAlternative("OAuth2", listOf("read:pets"))
        assertThat(plan("/protected")).isEqualTo(OAuth2SecurityPlan(listOf(read), tokenRequired = true))
        assertThat(plan("/anonymous")).isNull()
        assertThat(plan("/optional")).isEqualTo(OAuth2SecurityPlan(listOf(read), tokenRequired = false))
        assertThat(plan("/combined")).isNull()
        assertThat(plan("/alternative")).isEqualTo(OAuth2SecurityPlan(listOf(read), tokenRequired = false))
        assertThat(plan("/scope-alternatives")).isEqualTo(
            OAuth2SecurityPlan(listOf(read, OAuth2SecurityAlternative("OAuth2", listOf("write:pets"))), tokenRequired = true),
        )
        assertThat(
            plan("/unscoped"),
        ).isEqualTo(OAuth2SecurityPlan(listOf(OAuth2SecurityAlternative("OAuth2", emptyList())), tokenRequired = true))
        assertThat(plan("/schemes")).isEqualTo(
            OAuth2SecurityPlan(listOf(read, OAuth2SecurityAlternative("OtherOAuth2", listOf("write:pets"))), tokenRequired = true),
        )
    }
}
