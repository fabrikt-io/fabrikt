package examples.discriminatedOneOf.models

import jakarta.validation.Valid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NestedInlineContainerNested(
  @SerialName("detail")
  @get:Valid
  public val detail: NestedInlineContainerDetail? = null,
)
