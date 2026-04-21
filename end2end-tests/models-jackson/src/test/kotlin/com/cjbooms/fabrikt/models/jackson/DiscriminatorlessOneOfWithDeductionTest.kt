package com.cjbooms.fabrikt.models.jackson

import com.cjbooms.fabrikt.models.jackson.Helpers.mapper
import com.example.oneof.models.DiagnosticReport
import com.example.oneof.models.DiagnosticReportFailure
import com.example.oneof.models.DnsFailure
import com.example.oneof.models.NetworkFailure
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Demonstrates Jackson serde behaviour for a discriminator-less inline oneOf that opts into
 * `x-fabrikt-jackson-deduction: true`.
 *
 * Unlike [DiscriminatorlessOneOfTest] (where the property stays `Any?`), the property is typed as
 * the sealed super-interface [DiagnosticReportFailure]. Jackson dispatches to the correct subtype
 * by deduction — inspecting which properties are present in the JSON. Subtypes have distinguishing
 * required fields (`retries` vs `host`) so deduction is unambiguous.
 *
 * Constraint: subtypes must have distinguishing required properties for deduction to succeed; if
 * they don't, deserialization fails at runtime.
 */
class DiscriminatorlessOneOfWithDeductionTest {

    private val objectMapper = mapper()

    @Test
    fun `serialization uses runtime type - NetworkFailure unique field emitted`() {
        val report = DiagnosticReport(
            failure = NetworkFailure(kind = "network", retries = 3),
        )

        val json = objectMapper.writeValueAsString(report)

        assertThat(json).contains("\"kind\":\"network\"")
        assertThat(json).contains("\"retries\":3")
    }

    @Test
    fun `serialization uses runtime type - DnsFailure unique field emitted`() {
        val report = DiagnosticReport(
            failure = DnsFailure(kind = "dns", host = "example.com"),
        )

        val json = objectMapper.writeValueAsString(report)

        assertThat(json).contains("\"kind\":\"dns\"")
        assertThat(json).contains("\"host\":\"example.com\"")
    }

    @Test
    fun `deserialization deduces NetworkFailure from retries field`() {
        val json = """{"failure":{"kind":"network","retries":3}}"""

        val result = objectMapper.readValue(json, DiagnosticReport::class.java)

        val failure: DiagnosticReportFailure? = result.failure
        assertThat(failure).isInstanceOf(NetworkFailure::class.java)
        val network = failure as NetworkFailure
        assertThat(network.kind).isEqualTo("network")
        assertThat(network.retries).isEqualTo(3)
    }

    @Test
    fun `deserialization deduces DnsFailure from host field`() {
        val json = """{"failure":{"kind":"dns","host":"example.com"}}"""

        val result = objectMapper.readValue(json, DiagnosticReport::class.java)

        val failure: DiagnosticReportFailure? = result.failure
        assertThat(failure).isInstanceOf(DnsFailure::class.java)
        val dns = failure as DnsFailure
        assertThat(dns.kind).isEqualTo("dns")
        assertThat(dns.host).isEqualTo("example.com")
    }

    @Test
    fun `round-trip preserves runtime subtype and all fields`() {
        val original = DiagnosticReport(
            failure = NetworkFailure(kind = "network", retries = 7),
        )

        val json = objectMapper.writeValueAsString(original)
        val deserialized = objectMapper.readValue(json, DiagnosticReport::class.java)

        assertThat(deserialized.failure).isInstanceOf(NetworkFailure::class.java)
        assertThat(deserialized).isEqualTo(original)
    }
}
