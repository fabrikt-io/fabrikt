package examples.inLinedObject.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.String
import kotlin.collections.List

public data class ThirdInlineObjectGeneration(
  @param:JsonProperty("urls")
  @get:JsonProperty("urls")
  public val urls: List<@Valid ThirdInlineObjectUrls>? = null,
  @param:JsonProperty("view_name")
  @get:JsonProperty("view_name")
  public val viewName: String? = null,
)
