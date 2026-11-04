package examples.discriminatedOneOf.models

import jakarta.validation.Valid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NestedInlineContainerDetail(
  @SerialName("value")
  @get:Valid
  public val `value`: NestedInlineContainerValue? = null,
)
