package examples.enumPolymorphicDiscriminator.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.collections.List

public data class Responses(
  @param:JsonProperty("entries")
  @get:JsonProperty("entries")
  @get:Valid
  public val entries: List<ChildDefinition>? = null,
)
