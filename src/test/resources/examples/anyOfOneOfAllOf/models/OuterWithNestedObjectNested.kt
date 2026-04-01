package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class OuterWithNestedObjectNested(
  /**
   * Nested name - different property, should NOT be required
   */
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  public val name: String? = null,
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  public val id: String? = null,
)
