package examples.discriminatedOneOf.models

import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("obj2")
@Serializable
public data class Obj2(
  @SerialName("id2")
  @get:NotNull
  public val id2: String,
) : Poly1
