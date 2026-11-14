package com.cjbooms.fabrikt.clients

import tools.jackson.databind.exc.InvalidTypeIdException
import org.assertj.core.api.Assertions.assertThatThrownBy
import tools.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

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

}
