package examples.discriminatedOneOf.models

import jakarta.validation.Valid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NestedInlineContainerEntries(
  @SerialName("selection")
  @get:Valid
  public val selection: NestedInlineContainerSelection? = null,
)
