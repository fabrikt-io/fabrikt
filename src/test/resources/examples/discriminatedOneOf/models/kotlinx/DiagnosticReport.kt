package examples.discriminatedOneOf.models

import kotlin.Any
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class DiagnosticReport(
  @SerialName("failure")
  public val failure: Any? = null,
)
