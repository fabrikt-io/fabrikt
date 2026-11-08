package com.cjbooms.fabrikt.servers.spring

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.mock.web.MockServletContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

private const val VERSION_PARAMETER = "version"

private val CONTRIBUTOR_MEDIA_TYPE: MediaType = MediaType.parseMediaType("application/x.example.contributor+json")

/**
 * Spring Framework 7 resolves the API version from the `version` media-type parameter of the `Accept` header, which
 * makes the narrow `produces` of each generated method routable.
 */
@Configuration
@EnableWebMvc
open class MediaTypeApiVersioningConfig : WebMvcConfigurer {
    override fun configureApiVersioning(configurer: ApiVersionConfigurer) {
        configurer.useMediaTypeParameter(CONTRIBUTOR_MEDIA_TYPE, VERSION_PARAMETER)
    }
}

/**
 * Runtime proof for https://github.com/fabrikt-io/fabrikt/issues/840 (part B): two handlers generated from one
 * operation, distinguished only by `@RequestMapping(version = "...")`, route on the `version` media-type parameter.
 */
class VersionRoutingTest {
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val context =
            AnnotationConfigWebApplicationContext().apply {
                servletContext = MockServletContext()
                register(MediaTypeApiVersioningConfig::class.java, ContributorControllerImpl::class.java)
            }
        context.refresh()
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `routes a version=1 accept header to the v1 handler`() {
        mockMvc
            .perform(get("/contributors/1").accept("$CONTRIBUTOR_MEDIA_TYPE;version=1"))
            .andExpect(status().isOk)
            .andExpect(content().json("""{"id":"1","name":"Ada"}"""))
    }

    @Test
    fun `routes a version=2 accept header to the v2 handler`() {
        mockMvc
            .perform(get("/contributors/1").accept("$CONTRIBUTOR_MEDIA_TYPE;version=2"))
            .andExpect(status().isOk)
            .andExpect(content().json("""{"id":"1","name":"Ada","role":"author"}"""))
    }
}
