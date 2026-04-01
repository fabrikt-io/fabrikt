package examples.modelSuffix.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Int

public data class VariantBDto(
  @param:JsonProperty("fieldB")
  @get:JsonProperty("fieldB")
  public val fieldB: Int? = null,
) : OneOfTypeDto
