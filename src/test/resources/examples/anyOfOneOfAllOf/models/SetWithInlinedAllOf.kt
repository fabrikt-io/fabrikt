package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import java.util.LinkedHashSet
import javax.validation.Valid

public data class SetWithInlinedAllOf(
  @param:JsonProperty("items")
  @get:JsonProperty("items")
  @get:Valid
  public val items: LinkedHashSet<SetWithInlinedAllOfItems>? = null,
)
