package com.cjbooms.fabrikt.models.jackson

import com.cjbooms.fabrikt.models.jackson.Helpers.mapper
import com.example.oneof.models.DiagnosticReport
import com.example.oneof.models.DiagnosticReportFailure
import com.example.oneof.models.DnsFailure
import com.example.oneof.models.NetworkFailure
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/** Counterpart to [DiscriminatorlessOneOfTest]: opting into `x-jackson-subtype-deduction`
 *  types the property as the sealed interface and lets Jackson dispatch by required-property
 *  shape (`retries` vs `host`). */
class DiscriminatorlessOneOfWithDeductionTest {

    private val objectMapper = mapper()

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
