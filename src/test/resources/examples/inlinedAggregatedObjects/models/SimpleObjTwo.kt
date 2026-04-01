package examples.inlinedAggregatedObjects.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class SimpleObjTwo(
  @param:JsonProperty("companyName")
  @get:JsonProperty("companyName")
  @get:NotNull
  public val companyName: String,
)
