package examples.normalizedNameConflation.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class Child(
  @param:JsonProperty("type")
  @get:JsonProperty("type")
  public val type: String? = null,
  @param:JsonProperty("value")
  @get:JsonProperty("value")
  public val `value`: String? = null,
  @get:JsonProperty("${'$'}type")
  @get:NotNull
  @param:JsonProperty("${'$'}type")
  override val `$type`: String = "child",
) : Grandparent()
