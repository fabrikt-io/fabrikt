package examples.jsonSchemaConversion.eventType.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import kotlin.String

public data class Money(
  @param:JsonProperty("amount")
  @get:JsonProperty("amount")
  @get:NotNull
  public val amount: BigDecimal,
  @param:JsonProperty("currency")
  @get:JsonProperty("currency")
  @get:NotNull
  public val currency: String,
)
