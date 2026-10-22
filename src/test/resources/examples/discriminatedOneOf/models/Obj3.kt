package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class Obj3(
  @param:JsonProperty("id3")
  @get:JsonProperty("id3")
  @get:NotNull
  public val id3: String,
) : Poly2 {
  @get:JsonProperty(
    value = "type",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  public val type: String
    get() = "obj3"
}
