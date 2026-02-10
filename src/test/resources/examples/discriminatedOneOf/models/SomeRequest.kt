package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import java.math.BigDecimal
import javax.validation.Valid
import kotlin.collections.List

public data class SomeRequest(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  public val id: BigDecimal? = null,
  @param:JsonProperty("events")
  @get:JsonProperty("events")
  @get:Valid
  public val events: List<Test>? = null,
)
