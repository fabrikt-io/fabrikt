package examples.jsonSchemaConversion.eventType.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.Any
import kotlin.collections.List

public data class OffersConfig(
  @param:JsonProperty("total")
  @get:JsonProperty("total")
  @get:NotNull
  @get:Valid
  public val total: Money,
  @param:JsonProperty("reference_amount")
  @get:JsonProperty("reference_amount")
  @get:Valid
  public val referenceAmount: ReferenceAmount? = null,
  @param:JsonProperty("state")
  @get:JsonProperty("state")
  @get:NotNull
  public val state: State,
  @param:JsonProperty("campaigns")
  @get:JsonProperty("campaigns")
  @get:Valid
  public val campaigns: List<Campaign>? = null,
  @param:JsonProperty("release_restrictions")
  @get:JsonProperty("release_restrictions")
  public val releaseRestrictions: Any? = null,
  @param:JsonProperty("threshold")
  @get:JsonProperty("threshold")
  @get:Valid
  public val threshold: Threshold? = null,
)
