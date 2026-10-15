package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ClientHeaderApiKeySecurityTest {
    private val spec = readTextResource("/examples/headerApiKeySecurity/api.yaml")
    private val options =
        setOf(
            ClientCodeGenOptionType.OPENAPI_API_KEY_AUTHENTICATION,
            ClientCodeGenOptionType.OPENAPI_OAUTH2_AUTHENTICATION,
            ClientCodeGenOptionType.OPENAPI_BEARER_AUTHENTICATION,
        )

    @ParameterizedTest
    @ValueSource(strings = ["3.0.4", "3.1.1", "3.2.0"])
    fun `requirements retain alternatives combinations scopes and overrides`(version: String) {
        val document = SourceApi(spec.replace("3.0.4", version)).openApi3
        val resolver = ClientHeaderApiKeySecurity(document)

        fun plans(verb: String) =
            resolver.forOperation(
                document.paths
                    .getValue("/documents")
                    .operations
                    .getValue(verb),
                options,
            )!!
        assertThat(plans("get").map { it.kinds }).containsExactly(listOf(HeaderCredentialKind.API_KEY), listOf(HeaderCredentialKind.OAUTH2))
        assertThat(
            plans("post")
                .single()
                .alternatives
                .single()
                .map { it.scheme },
        ).containsExactly("Key", "OAuth")
        assertThat(
            plans("post")
                .single()
                .alternatives
                .single()
                .last()
                .scopes,
        ).containsExactly("write:documents")
        assertThat(
            plans("put")
                .single()
                .alternatives
                .single()
                .single()
                .header,
        ).isEqualTo("X-API-Key")
        assertThat(
            resolver.forOperation(
                document.paths
                    .getValue("/documents")
                    .operations
                    .getValue("delete"),
                options,
            ),
        ).isNull()
        assertThat(
            plans("patch").single().alternatives.map {
                it.last().scopes
            },
        ).containsExactly(listOf("write:documents"), listOf("admin:documents"))
        assertThat(
            resolver.forOperation(
                document.paths
                    .getValue("/documents")
                    .operations
                    .getValue("get"),
                emptySet(),
            ),
        ).isNull()
    }

    @Test
    fun `unsupported schemes disabled options and header conflicts never produce partial helpers`() {
        listOf(
            spec.replace("name: X-API-Key", "name: authorization"),
            spec.replace("name: X-API-Key", "name: 'invalid header'"),
            spec.replace("in: header", "in: query"),
            spec.replace("type: oauth2", "type: http\n      scheme: basic"),
        ).forEach { source ->
            val document = SourceApi(source).openApi3
            assertThat(
                ClientHeaderApiKeySecurity(
                    document,
                ).forOperation(
                    document.paths
                        .getValue("/documents")
                        .operations
                        .getValue("post"),
                    options,
                ),
            ).isEmpty()
        }
        val document = SourceApi(spec).openApi3
        assertThat(
            ClientHeaderApiKeySecurity(
                document,
            ).forOperation(
                document.paths
                    .getValue("/documents")
                    .operations
                    .getValue("post"),
                setOf(ClientCodeGenOptionType.OPENAPI_API_KEY_AUTHENTICATION),
            ),
        ).isEmpty()
    }
}
