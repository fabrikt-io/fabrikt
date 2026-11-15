package com.cjbooms.fabrikt.clients

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import tools.jackson.databind.exc.InvalidTypeIdException
import tools.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.kotlinFunction

class SingletonHierarchyUnionTest {
    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun singletonReferencesDeserializeConcreteValues(openParents: Boolean) {
        val variant = if (openParents) "openparents" else "sealedparents"
        val target = Class.forName("com.example.singleton." + variant + ".models.Holder")
        val child = """{"kind":"child","id":"1","label":"Alice"}"""
        val mapper = jacksonObjectMapper()
        val result = mapper.readValue("""{"single":$child,"values":[$child]}""", target)
        val single = target.getMethod("getSingle").invoke(result)
        val values = target.getMethod("getValues").invoke(result) as List<*>
        assertThat(single.javaClass.simpleName).isEqualTo("Child")
        assertThat(single.javaClass.getMethod("getLabel").invoke(single)).isEqualTo("Alice")
        assertThat(values).containsExactly(single)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun intermediateContractsRoundTripTheirBranch(openParents: Boolean) {
        val variant = if (openParents) "openparents" else "sealedparents"
        val target = Class.forName("com.example.singleton." + variant + ".models.Holder")
        val contract = target.getMethod("getDirect").returnType
        assertThat(contract.simpleName).isEqualTo("MiddleContract")
        val mapper = jacksonObjectMapper()
        for ((kind, extra) in listOf("middle" to "", "child" to ""","label":"Alice"""")) {
            val json = """{"kind":"$kind","id":"1"$extra}"""
            val result = mapper.readValue(json, contract)
            assertThat(result.javaClass.simpleName).isEqualTo(if (kind == "child") "Child" else "Middle")
            assertThat(mapper.readValue(mapper.writeValueAsString(result), contract)).isEqualTo(result)
            val wrapped = mapper.readValue("""{"direct":$json,"directValues":[$json],"directMap":{"entry":$json},"bag":{"entry":$json}}""", target)
            assertThat(mapper.readValue(mapper.writeValueAsString(wrapped), target)).isEqualTo(wrapped)
        }
        assertThatThrownBy { mapper.readValue("""{"kind":"third","id":"1"}""", contract) }.isInstanceOf(InvalidTypeIdException::class.java)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun generatedClientUsesContractsForBodiesAndParameters(openParents: Boolean) {
        val variant = if (openParents) "openparents" else "sealedparents"
        val prefix = "com.example.singleton.$variant"
        val contract = Class.forName("$prefix.models.MiddleContract")
        val api = Class.forName("$prefix.client.MiddleClient")
        val method = api.declaredMethods.single { !it.isSynthetic && it.parameterTypes.count { type -> type == contract } == 2 }
        assertThat(method.genericReturnType.typeName).contains(contract.name)
        val mapper = jacksonObjectMapper()
        val json = """{"kind":"child","id":"1","label":"Alice"}"""
        val child = mapper.readValue(json, contract)
        val server = WireMockServer(options().dynamicPort())
        server.start()
        try {
            server.stubFor(post(urlPathEqualTo("/middle")).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)))
            val instance =
                api.getConstructor(mapper.javaClass, String::class.java, OkHttpClient::class.java)
                    .newInstance(mapper, server.baseUrl(), OkHttpClient())
            val function = checkNotNull(method.kotlinFunction)
            val required =
                function.parameters.filterNot { it.isOptional }.associateWith {
                    if (it.kind == KParameter.Kind.INSTANCE) instance else child
                }
            val response = checkNotNull(function.callBy(required))
            val data = response.javaClass.getMethod("getData").invoke(response)
            assertThat(data.javaClass.simpleName).isEqualTo("Child")
            assertThat(data.javaClass.getMethod("getLabel").invoke(data)).isEqualTo("Alice")
            server.verify(postRequestedFor(urlPathEqualTo("/middle")).withRequestBody(matchingJsonPath("$.label", equalTo("Alice"))))
        } finally {
            server.stop()
        }
    }
}
