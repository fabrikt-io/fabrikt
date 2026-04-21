package examples.discriminatedOneOf.models

import jakarta.validation.Valid
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ResponseWithChildActionsPartial(
  @SerialName("actions")
  @get:Valid
  public val actions: List<ChildActionsPartial>? = null,
)
