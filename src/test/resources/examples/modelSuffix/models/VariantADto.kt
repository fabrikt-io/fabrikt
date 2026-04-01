package examples.modelSuffix.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class VariantADto(
  @param:JsonProperty("fieldA")
  @get:JsonProperty("fieldA")
  public val fieldA: String? = null,
) : OneOfTypeDto
