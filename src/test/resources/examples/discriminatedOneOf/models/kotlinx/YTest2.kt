package examples.discriminatedOneOf.models

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("Y2")
@Serializable
public data class YTest2(
  @SerialName("alt")
  public val alt: String? = null,
) : YTest
