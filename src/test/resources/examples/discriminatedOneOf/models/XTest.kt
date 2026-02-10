package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class XTest(
  @get:JsonProperty("type")
  public val type: String? = null,
  @get:JsonProperty("alt")
  public val alt: String? = null,
) : Test
