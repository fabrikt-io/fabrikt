package com.cjbooms.fabrikt.models.jackson

import com.cjbooms.fabrikt.models.jackson.Helpers.mapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RedundantHierarchyUnionTest {
    private val mapper = mapper()

    @Test
    fun `compiled wrapper properties deserialize through explicit discriminator mappings`() {
        listOf(
            "first" to com.example.redundanthierarchyunion.models.First::class.java,
            "second" to com.example.redundanthierarchyunion.models.Second::class.java,
        ).forEach { (kind, expectedType) ->
            val value = """{"kind":"$kind","id":"example","enabled":true}"""
            val json = """{"baseFirst":$value,"baseMiddle":$value,"baseLast":$value,"values":[$value,null],"named":$value}"""
            val decoded = mapper.readValue(json, com.example.redundanthierarchyunion.models.Wrapper::class.java)

            assertThat(decoded.baseFirst).isInstanceOf(expectedType)
            assertThat(decoded.baseMiddle).isInstanceOf(expectedType)
            assertThat(decoded.baseLast).isInstanceOf(expectedType)
            assertThat(decoded.named).isInstanceOf(expectedType)
            assertThat(decoded.values?.first()).isInstanceOf(expectedType)
            assertThat(decoded.values?.last()).isNull()
            assertThat(mapper.readValue(mapper.writeValueAsString(decoded), com.example.redundanthierarchyunion.models.Wrapper::class.java)).isEqualTo(decoded)
        }
    }

    @Test
    fun `compiled wrapper properties deserialize through explicit mappings with shared composition contracts`() {
        listOf(
            "first" to com.example.redundanthierarchyunion.contracts.models.First::class.java,
            "second" to com.example.redundanthierarchyunion.contracts.models.Second::class.java,
        ).forEach { (kind, expectedType) ->
            val value = """{"kind":"$kind","id":"example","enabled":true}"""
            val json = """{"baseFirst":$value,"baseMiddle":$value,"baseLast":$value,"values":[$value,null],"named":$value}"""
            val decoded = mapper.readValue(json, com.example.redundanthierarchyunion.contracts.models.Wrapper::class.java)

            assertThat(decoded.baseFirst).isInstanceOf(expectedType)
            assertThat(decoded.baseFirst).isInstanceOf(com.example.redundanthierarchyunion.contracts.models.BaseComposite::class.java)
            assertThat(decoded.baseMiddle).isInstanceOf(expectedType)
            assertThat(decoded.baseLast).isInstanceOf(expectedType)
            assertThat(decoded.named).isInstanceOf(expectedType)
            assertThat(decoded.values?.first()).isInstanceOf(expectedType)
            assertThat(decoded.values?.last()).isNull()
            assertThat(mapper.readValue(mapper.writeValueAsString(decoded), com.example.redundanthierarchyunion.contracts.models.Wrapper::class.java)).isEqualTo(decoded)
        }
    }

    @Test
    fun `compiled wrapper properties deserialize through implicit discriminator mappings`() {
        listOf(
            "First" to com.example.redundanthierarchyunion.implicit.models.First::class.java,
            "Second" to com.example.redundanthierarchyunion.implicit.models.Second::class.java,
        ).forEach { (kind, expectedType) ->
            val value = """{"kind":"$kind","id":"example","enabled":true}"""
            val json = """{"baseFirst":$value,"baseMiddle":$value,"baseLast":$value,"values":[$value,null],"named":$value}"""
            val decoded = mapper.readValue(json, com.example.redundanthierarchyunion.implicit.models.Wrapper::class.java)

            assertThat(decoded.baseFirst).isInstanceOf(expectedType)
            assertThat(decoded.baseMiddle).isInstanceOf(expectedType)
            assertThat(decoded.baseLast).isInstanceOf(expectedType)
            assertThat(decoded.named).isInstanceOf(expectedType)
            assertThat(decoded.values?.first()).isInstanceOf(expectedType)
            assertThat(decoded.values?.last()).isNull()
            assertThat(mapper.readValue(mapper.writeValueAsString(decoded), com.example.redundanthierarchyunion.implicit.models.Wrapper::class.java)).isEqualTo(decoded)
        }
    }

    @Test
    fun `compiled wrapper properties deserialize through implicit mappings with shared composition contracts`() {
        listOf(
            "First" to com.example.redundanthierarchyunion.implicit.contracts.models.First::class.java,
            "Second" to com.example.redundanthierarchyunion.implicit.contracts.models.Second::class.java,
        ).forEach { (kind, expectedType) ->
            val value = """{"kind":"$kind","id":"example","enabled":true}"""
            val json = """{"baseFirst":$value,"baseMiddle":$value,"baseLast":$value,"values":[$value,null],"named":$value}"""
            val decoded = mapper.readValue(json, com.example.redundanthierarchyunion.implicit.contracts.models.Wrapper::class.java)

            assertThat(decoded.baseFirst).isInstanceOf(expectedType)
            assertThat(decoded.baseFirst).isInstanceOf(com.example.redundanthierarchyunion.implicit.contracts.models.BaseComposite::class.java)
            assertThat(decoded.baseMiddle).isInstanceOf(expectedType)
            assertThat(decoded.baseLast).isInstanceOf(expectedType)
            assertThat(decoded.named).isInstanceOf(expectedType)
            assertThat(decoded.values?.first()).isInstanceOf(expectedType)
            assertThat(decoded.values?.last()).isNull()
            assertThat(mapper.readValue(mapper.writeValueAsString(decoded), com.example.redundanthierarchyunion.implicit.contracts.models.Wrapper::class.java)).isEqualTo(decoded)
        }
    }
}
