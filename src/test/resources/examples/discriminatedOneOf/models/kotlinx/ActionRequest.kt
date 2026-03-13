package examples.discriminatedOneOf.models

import jakarta.validation.Valid
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ActionRequest(
  @SerialName("id")
  public val id: String? = null,
  @SerialName("action")
  @get:Valid
  public val action: ParentAction? = null,
)
