package examples.arrays.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ContainsEnum(
  @SerialName("somes")
  public val somes: SomeEnum? = null,
)
