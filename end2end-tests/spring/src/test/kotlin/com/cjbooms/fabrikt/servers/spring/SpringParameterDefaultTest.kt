package com.cjbooms.fabrikt.servers.spring

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * End-to-end coverage for https://github.com/fabrikt-io/fabrikt/issues/612 against a running Spring
 * MVC stack: a `required: false` parameter that declares a `default` is always populated by Spring,
 * so its generated Kotlin type is non-nullable and the handler receives the default when the caller
 * omits it.
 */
@WebMvcTest
@ContextConfiguration(classes = [ParameterDefaultTestConfig::class])
class SpringParameterDefaultTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `default header and query values are injected when the caller omits them`() {
        mockMvc.perform(get("/example"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.param1").value("paramValue"))
            .andExpect(jsonPath("$.limit").value(10))
    }

    @Test
    fun `explicit values override the defaults`() {
        mockMvc.perform(get("/example?limit=42").header("param1", "custom"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.param1").value("custom"))
            .andExpect(jsonPath("$.limit").value(42))
    }

    @Test
    fun `an optional parameter without a default is absent when omitted`() {
        mockMvc.perform(get("/example"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.filter").doesNotExist())
    }
}

@Configuration
open class ParameterDefaultTestConfig {
    @Bean
    open fun exampleController() = ExampleControllerImpl()
}
