package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class Obj2(
  @param:JsonProperty("id2")
  @get:JsonProperty("id2")
  @get:NotNull
  public val id2: String,
) : Poly1 {
  @get:JsonProperty(
    value = "type",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  public val type: String
    get() = "obj2"
}
