package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.String

public data class ActionRequest(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  public val id: String? = null,
  @param:JsonProperty("action")
  @get:JsonProperty("action")
  @get:Valid
  public val action: ParentAction? = null,
)
