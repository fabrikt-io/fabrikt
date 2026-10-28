package examples.singleAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.collections.List

public data class Example(
  @param:JsonProperty("direct")
  @get:JsonProperty("direct")
  @get:Valid
  public val direct: Person? = null,
  @param:JsonProperty("wrapped")
  @get:JsonProperty("wrapped")
  @get:Valid
  public val wrapped: Person? = null,
  @param:JsonProperty("wrappedRequired")
  @get:JsonProperty("wrappedRequired")
  @get:NotNull
  @get:Valid
  public val wrappedRequired: Person,
  @param:JsonProperty("people")
  @get:JsonProperty("people")
  public val people: List<@Valid Person?>? = null,
)
