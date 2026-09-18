package examples.jsonSchemaConversion.eventType.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import java.math.BigDecimal
import kotlin.String

public data class ReferenceAmount(
  @param:JsonProperty("amount")
  @get:JsonProperty("amount")
  public val amount: BigDecimal? = null,
  @param:JsonProperty("unit")
  @get:JsonProperty("unit")
  public val unit: String? = null,
)
