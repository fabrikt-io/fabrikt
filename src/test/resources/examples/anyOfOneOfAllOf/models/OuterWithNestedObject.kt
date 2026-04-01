package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class OuterWithNestedObject(
  /**
   * Outer name property
   */
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  @get:NotNull
  public val name: String,
  @param:JsonProperty("nested")
  @get:JsonProperty("nested")
  @get:Valid
  public val nested: OuterWithNestedObjectNested? = null,
)
