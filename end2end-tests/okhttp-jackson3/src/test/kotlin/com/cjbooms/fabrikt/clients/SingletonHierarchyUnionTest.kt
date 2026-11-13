package com.cjbooms.fabrikt.clients

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
}
