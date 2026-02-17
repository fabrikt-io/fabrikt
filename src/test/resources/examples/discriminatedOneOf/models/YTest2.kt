package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class YTest2(
  @param:JsonProperty("type")
  @get:JsonProperty("type")
  public val type: String? = null,
  @param:JsonProperty("alt")
  @get:JsonProperty("alt")
  public val alt: String? = null,
) : YTest
