package com.cjbooms.fabrikt.clients

import tools.jackson.databind.exc.InvalidTypeIdException
import tools.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SingletonHierarchyUnionTest {
    private val mapper = jacksonObjectMapper()
    private val child = """{"kind":"child","id":"1","label":"Alice","note":null,"middle":"retained"}"""

    private fun holder(openParents: Boolean): Class<*> {
        val variant = if (openParents) "openparents" else "sealedparents"
        return Class.forName("com.example.singleton." + variant + ".models.Holder")
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun singletonReferencesRetainTheirConcretePayloadAndMetadata(openParents: Boolean) {
        val target = holder(openParents)
        assertThat(target.getMethod("getSingle").returnType.simpleName).isEqualTo("Child")
        for (property in listOf("getParent", "getMiddle", "getSubset")) {
            assertThat(target.getMethod(property).returnType.simpleName).isEqualTo("Base")
        }
        val second = child.replace(""""id":"1"""", """"id":"2"""")
        val json = """{"single":$child,"values":[$child,$second],"nullableValues":[null,$child],"parent":$child,"middle":$child}"""
        val result = mapper.readValue(json, target)
        val encoded = mapper.readTree(mapper.writeValueAsString(result))
        assertThat(encoded.at("/single/label").stringValue()).isEqualTo("Alice")
        assertThat(encoded.at("/single/middle").stringValue()).isEqualTo("retained")
        assertThat(encoded.at("/single/note").isNull).isTrue()
        val values = encoded.get("values")
        assertThat((0 until values.size()).map { values.get(it).get("id").stringValue() }).containsExactly("1", "2")
        assertThat(encoded.at("/values/1/label").stringValue()).isEqualTo("Alice")
        assertThat(encoded.at("/nullableValues/0").isNull).isTrue()
        assertThat(encoded.at("/nullableValues/1/label").stringValue()).isEqualTo("Alice")
        val restored = mapper.readValue(mapper.writeValueAsString(result), target)
        assertThat(mapper.readTree(mapper.writeValueAsString(restored))).isEqualTo(encoded)
        val defaulted = mapper.readValue("""{"single":{"kind":"child","id":"1","label":"Alice"}}""", target)
        assertThat(mapper.readTree(mapper.writeValueAsString(defaulted)).at("/single/note").stringValue()).isEqualTo("hello")
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun singletonsAndExplicitSubsetsRejectUnlistedSiblings(openParents: Boolean) {
        val target = holder(openParents)
        for (json in listOf(
            """{"single":{"kind":"third","id":"1"}}""",
            """{"values":[{"kind":"third","id":"1"}]}""",
            """{"preciseSubset":{"kind":"third","id":"1"}}""",
        )) {
            assertThatThrownBy { mapper.readValue(json, target) }.isInstanceOf(InvalidTypeIdException::class.java)
        }
        val result = mapper.readValue("""{"subset":{"kind":"third","id":"1"}}""", target)
        assertThat(target.getMethod("getSubset").invoke(result).javaClass.simpleName).isEqualTo("Third")
    }
}
