package examples.discriminatedOneOf.models

import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("obj1")
@Serializable
public data class Obj1(
  @SerialName("id1")
  @get:NotNull
  public val id1: String,
) : Poly1, Poly2
