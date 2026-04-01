package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class TopLevelRequiredWithAllOf(
  @param:JsonProperty("inline_prop")
  @get:JsonProperty("inline_prop")
  @get:NotNull
  public val inlineProp: String,
  @param:JsonProperty("top_level_only_prop")
  @get:JsonProperty("top_level_only_prop")
  @get:NotNull
  public val topLevelOnlyProp: String,
  @param:JsonProperty("optional_prop")
  @get:JsonProperty("optional_prop")
  public val optionalProp: String? = null,
)
