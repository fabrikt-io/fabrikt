package examples.instantDateTime.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.collections.List

public data class QueryResult(
  @param:JsonProperty("items")
  @get:JsonProperty("items")
  @get:NotNull
  @get:Size(min = 0)
  @get:Valid
  public val items: List<FirstModel>,
)
