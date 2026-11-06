package examples.boundedModelNameCollisions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public data class NameCollisionHolder(
  @param:JsonProperty("first")
  @get:JsonProperty("first")
  @get:NotNull
  @get:Valid
  public val first: FooBar,
  @param:JsonProperty("second")
  @get:JsonProperty("second")
  @get:NotNull
  @get:Valid
  public val second: FooBarExtra,
  @param:JsonProperty("items")
  @get:JsonProperty("items")
  public val items: List<@Valid FooBar>? = null,
  @param:JsonProperty("byName")
  @get:JsonProperty("byName")
  public val byName: Map<String, @Valid FooBarExtra?>? = null,
)
