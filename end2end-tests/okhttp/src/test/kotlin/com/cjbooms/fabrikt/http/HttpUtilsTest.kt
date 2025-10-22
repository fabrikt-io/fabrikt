package com.cjbooms.fabrikt.http

import com.example.client.formParam
import com.example.client.pathParam
import com.example.client.queryParam
import com.example.models.ContentModelType
import okhttp3.FormBody
import okhttp3.HttpUrl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HttpUtilsTest {

    @Test
    fun `query parameters correctly handle both simple and custom enums`() {
        val urlBuilder = HttpUrl.Builder()
            .scheme("https")
            .host("test.com")

        urlBuilder.queryParam("first", ContentModelType.FIRST_MODEL)
        urlBuilder.queryParam("second", ContentModelType.SECOND_MODEL)
        urlBuilder.queryParam("list", listOf(ContentModelType.FIRST_MODEL, ContentModelType.SECOND_MODEL), explode = false)

        val url = urlBuilder.build()

        assertThat(url.queryParameter("first")).isEqualTo("first_model")
        assertThat(url.queryParameter("second")).isEqualTo("second_model")
        assertThat(url.queryParameter("list")).isEqualTo("first_model,second_model")
    }

    @Test
    fun `form parameters correctly handle both simple and custom enums`() {
        val formBuilder = FormBody.Builder()

        formBuilder.formParam("first", ContentModelType.FIRST_MODEL)
        formBuilder.formParam("second", ContentModelType.SECOND_MODEL)

        val form = formBuilder.build()

        assertThat(form.name(0)).isEqualTo("first")
        assertThat(form.value(0)).isEqualTo("first_model")
        assertThat(form.name(1)).isEqualTo("second")
        assertThat(form.value(1)).isEqualTo("second_model")
    }

    @Test
    fun `path parameters correctly handle both simple and custom enums`() {
        val path = "/api/{first}/test/{second}"

        val result = path.pathParam(
            "{first}" to ContentModelType.FIRST_MODEL,
            "{second}" to ContentModelType.SECOND_MODEL
        )

        assertThat(result).isEqualTo("/api/first_model/test/second_model")
    }
}
