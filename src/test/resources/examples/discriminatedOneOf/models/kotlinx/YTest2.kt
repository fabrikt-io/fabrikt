package examples.discriminatedOneOf.models

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class YTest2(
  @SerialName("type")
  public val type: String? = null,
  @SerialName("alt")
  public val alt: String? = null,
) : YTest
