package examples.redundantHierarchyUnion.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.collections.List

public data class Wrapper(
  @param:JsonProperty("baseFirst")
  @get:JsonProperty("baseFirst")
  @get:Valid
  public val baseFirst: Base? = null,
  @param:JsonProperty("baseMiddle")
  @get:JsonProperty("baseMiddle")
  @get:Valid
  public val baseMiddle: Base? = null,
  @param:JsonProperty("baseLast")
  @get:JsonProperty("baseLast")
  @get:Valid
  public val baseLast: Base? = null,
  @param:JsonProperty("values")
  @get:JsonProperty("values")
  public val values: List<@Valid Base?>? = null,
  @param:JsonProperty("named")
  @get:JsonProperty("named")
  @get:Valid
  public val named: Base? = null,
)
