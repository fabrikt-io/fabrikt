package examples.normalizedNameConflation.models

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("child")
@Serializable
public data class Child(
  @SerialName("type")
  public val type: String? = null,
  @SerialName("value")
  public val `value`: String? = null,
) : Grandparent()
