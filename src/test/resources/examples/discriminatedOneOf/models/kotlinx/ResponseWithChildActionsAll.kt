package examples.discriminatedOneOf.models

import jakarta.validation.Valid
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ResponseWithChildActionsAll(
  @SerialName("actions")
  @get:Valid
  public val actions: List<ChildActionsAll>? = null,
)
