package examples.normalizedNameConflation.models

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("parent")
@Serializable
public data class Parent(
  @SerialName("type")
  public val type: String? = null,
) : Grandparent()
