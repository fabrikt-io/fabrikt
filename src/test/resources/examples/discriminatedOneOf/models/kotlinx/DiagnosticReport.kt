package examples.discriminatedOneOf.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class DiagnosticReport(
  @SerialName("failure")
  public val failure: JsonElement? = null,
)
