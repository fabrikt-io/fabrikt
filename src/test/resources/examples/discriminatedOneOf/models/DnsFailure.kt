package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class DnsFailure(
  @param:JsonProperty("kind")
  @get:JsonProperty("kind")
  @get:NotNull
  public val kind: String,
  @param:JsonProperty("host")
  @get:JsonProperty("host")
  @get:NotNull
  public val host: String,
) : DiagnosticReportFailure
