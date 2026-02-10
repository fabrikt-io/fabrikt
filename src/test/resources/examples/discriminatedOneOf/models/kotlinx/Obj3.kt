package examples.discriminatedOneOf.models

import javax.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("obj3")
@Serializable
public data class Obj3(
  @SerialName("id3")
  @get:NotNull
  public val id3: String,
) : Poly2
