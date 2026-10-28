package examples.arrays.models

import jakarta.validation.Valid
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ContainsNamedInlinedArray(
  @SerialName("items")
  public val items: List<@Valid ArrayContainingComplexInlined>? = null,
)
