package com.cjbooms.fabrikt.models.jackson

import com.cjbooms.fabrikt.models.jackson.Helpers.mapper
import com.example.models.OrderChild
import com.example.models.OrderHolder
import com.example.models.OrderSibling
import com.example.models.OrderX
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AllOfParentOrderTest {
    private val objectMapper = mapper()

    @Test
    fun `common parent resolves after inline allOf branches including collection elements`() {
        val json = """{"value":{"id":"1","kind":"child","label":"hello"},"choice":{"id":"2","kind":"sibling","count":3},"choices":[{"id":"3","kind":"child","label":"world"}]}"""
        val result = objectMapper.readValue(json, OrderHolder::class.java)

        val choice: OrderX = result.choice
        assertThat(choice).isInstanceOf(OrderSibling::class.java)
        assertThat((choice as OrderSibling).count).isEqualTo(3)
        val element: OrderX = result.choices!!.single()
        assertThat(element).isInstanceOf(OrderChild::class.java)
        assertThat((element as OrderChild).label).isEqualTo("world")
        assertThat(objectMapper.readTree(objectMapper.writeValueAsString(result)))
            .isEqualTo(objectMapper.readTree(json))
    }
}
