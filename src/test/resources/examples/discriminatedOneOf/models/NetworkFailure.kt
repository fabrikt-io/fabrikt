package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int
import kotlin.String

public data class NetworkFailure(
  @param:JsonProperty("kind")
  @get:JsonProperty("kind")
  @get:NotNull
  public val kind: String,
  @param:JsonProperty(
    "retries",
    required = true,
  )
  @get:JsonProperty("retries")
  @get:NotNull
  public val retries: Int,
) : DiagnosticReportFailure
