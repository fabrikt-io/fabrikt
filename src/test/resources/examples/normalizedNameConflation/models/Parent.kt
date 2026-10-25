package examples.normalizedNameConflation.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class Parent(
  @param:JsonProperty("type")
  @get:JsonProperty("type")
  public val type: String? = null,
  @get:JsonProperty("${'$'}type")
  @get:NotNull
  @param:JsonProperty("${'$'}type")
  override val `$type`: String = "parent",
) : Grandparent()
