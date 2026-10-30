package com.cjbooms.fabrikt.models.jackson

import com.cjbooms.fabrikt.models.jackson.Helpers.mapper
import com.example.redundanthierarchyunion.models.First
import com.example.redundanthierarchyunion.models.Second
import com.example.redundanthierarchyunion.models.Wrapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RedundantHierarchyUnionTest {
    private val mapper = mapper()

    @Test
    fun `compiled wrapper properties deserialize through the existing discriminator`() {
        listOf("first" to First::class.java, "second" to Second::class.java).forEach { (kind, expectedType) ->
            val value = """{"kind":"$kind","id":"example","enabled":true}"""
            val json = """{"baseFirst":$value,"baseMiddle":$value,"baseLast":$value,"values":[$value,null],"named":$value}"""
            val decoded = mapper.readValue(json, Wrapper::class.java)

            assertThat(decoded.baseFirst).isInstanceOf(expectedType)
            assertThat(decoded.baseMiddle).isInstanceOf(expectedType)
            assertThat(decoded.baseLast).isInstanceOf(expectedType)
            assertThat(decoded.named).isInstanceOf(expectedType)
            val firstElement = decoded.values?.first()
            assertThat(firstElement).isInstanceOf(expectedType)
            assertThat(decoded.values?.last()).isNull()
            assertThat(mapper.readValue(mapper.writeValueAsString(decoded), Wrapper::class.java)).isEqualTo(decoded)
        }
    }
}
