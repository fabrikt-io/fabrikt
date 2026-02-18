package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class Obj3(
  @param:JsonProperty("id3")
  @get:JsonProperty("id3")
  @get:NotNull
  public val id3: String,
  @get:JsonProperty("type")
  @get:NotNull
  @param:JsonProperty("type")
  public val type: String = "obj3",
) : Poly2
