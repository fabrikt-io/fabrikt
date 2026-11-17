package examples.allOfParentOrder.models

import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("quiet")
@Serializable
public data class QuietSibling(
  @SerialName("id")
  @get:NotNull
  override val id: String,
  @SerialName("name")
  public val name: String? = null,
) : UnconfiguredParent()
