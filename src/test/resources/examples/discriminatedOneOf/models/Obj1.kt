package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull
import kotlin.String

public data class Obj1(
  @get:JsonProperty("id1")
  @get:NotNull
  public val id1: String,
  @get:JsonProperty("type")
  @get:NotNull
  @param:JsonProperty("type")
  public val type: String = "obj1",
) : Poly1, Poly2
