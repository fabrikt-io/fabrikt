package examples.customObjectMapping.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import examples.customObjectMapping.Money
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.collections.List

public data class Order(
  @param:JsonProperty("total")
  @get:JsonProperty("total")
  @get:NotNull
  @get:Valid
  public val total: Money,
  @param:JsonProperty("line_totals")
  @get:JsonProperty("line_totals")
  @get:NotNull
  public val lineTotals: List<@Valid Money>,
)
