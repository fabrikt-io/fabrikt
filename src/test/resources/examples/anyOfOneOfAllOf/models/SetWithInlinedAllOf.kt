package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import java.util.LinkedHashSet

public data class SetWithInlinedAllOf(
  @param:JsonProperty("items")
  @get:JsonProperty("items")
  public val items: LinkedHashSet<@Valid SetWithInlinedAllOfItems>? = null,
)
