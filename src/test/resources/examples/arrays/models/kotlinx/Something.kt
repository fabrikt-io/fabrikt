package examples.arrays.models

import kotlin.Int
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Something(
  @SerialName("some_value")
  public val someValue: Int? = null,
)
