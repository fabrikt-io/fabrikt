package examples.discriminatedOneOf.models

import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class DnsFailure(
  @SerialName("kind")
  @get:NotNull
  public val kind: String,
  @SerialName("host")
  @get:NotNull
  public val host: String,
) : DiagnosticReportFailure
