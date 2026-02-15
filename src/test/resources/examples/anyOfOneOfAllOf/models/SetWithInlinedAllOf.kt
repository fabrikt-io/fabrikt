package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import java.util.LinkedHashSet

public data class SetWithInlinedAllOf(
  @param:JsonProperty("items")
  @get:JsonProperty("items")
  @get:Valid
  public val items: LinkedHashSet<SetWithInlinedAllOfItems>? = null,
)
