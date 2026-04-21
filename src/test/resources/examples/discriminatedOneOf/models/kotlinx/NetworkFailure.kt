package examples.discriminatedOneOf.models

import jakarta.validation.constraints.NotNull
import kotlin.Int
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NetworkFailure(
  @SerialName("kind")
  @get:NotNull
  public val kind: String,
  @SerialName("retries")
  @get:NotNull
  public val retries: Int,
) : DiagnosticReportFailure
