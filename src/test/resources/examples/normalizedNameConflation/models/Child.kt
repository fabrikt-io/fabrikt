package examples.normalizedNameConflation.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class Child(
  @param:JsonProperty("${'$'}type")
  @get:JsonProperty("${'$'}type")
  @get:NotNull
  public val `$type`: String,
  @param:JsonProperty("type")
  @get:JsonProperty("type")
  public val type: String? = null,
  @param:JsonProperty("value")
  @get:JsonProperty("value")
  public val `value`: String? = null,
)
