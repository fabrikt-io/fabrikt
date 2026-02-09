package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull
import kotlin.String

public data class YTest2(
  @get:JsonProperty("alt")
  public val alt: String? = null,
  @get:JsonProperty("type")
  @get:NotNull
  @param:JsonProperty("type")
  public val type: YTest2Type = YTest2Type.Y2,
) : YTest
