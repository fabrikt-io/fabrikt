package examples.discriminatedOneOf.models

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("Y1")
@Serializable
public data class YTest1(
  @SerialName("alt")
  public val alt: String? = null,
) : YTest
