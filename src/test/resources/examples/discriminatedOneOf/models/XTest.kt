package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class XTest(
  @param:JsonProperty("alt")
  @get:JsonProperty("alt")
  public val alt: String? = null,
  @param:JsonProperty("type")
  @get:JsonProperty("type")
  @get:NotNull
  public val type: String = "XTest",
) : Test
