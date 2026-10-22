package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class Obj1(
  @param:JsonProperty("id1")
  @get:JsonProperty("id1")
  @get:NotNull
  public val id1: String,
) : Poly1,
    Poly2 {
  @get:JsonProperty(
    value = "type",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  public val type: String
    get() = "obj1"
}
