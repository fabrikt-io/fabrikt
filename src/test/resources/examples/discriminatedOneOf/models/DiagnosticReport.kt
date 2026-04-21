package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid

public data class DiagnosticReport(
  @param:JsonProperty("failure")
  @get:JsonProperty("failure")
  @get:Valid
  public val failure: DiagnosticReportFailure? = null,
)
