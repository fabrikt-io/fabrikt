package examples.wildCardTypes.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Any
import kotlin.String
import kotlin.collections.Map

public data class WildCardTypes(
  @param:JsonProperty("user_name")
  @get:JsonProperty("user_name")
  @get:NotNull
  public val userName: String,
  @param:JsonProperty("meta")
  @get:JsonProperty("meta")
  @get:NotNull
  public val meta: Map<String, Any?>,
)
