package examples.discriminatedOneOf.models

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("X")
@Serializable
public data class XTest(
  @SerialName("alt")
  public val alt: String? = null,
) : Test
