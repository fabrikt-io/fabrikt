package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull
import kotlin.String

public data class Obj2(
  @get:JsonProperty("id2")
  @get:NotNull
  public val id2: String,
  @get:JsonProperty("type")
  @get:NotNull
  @param:JsonProperty("type")
  public val type: String = "obj2",
) : Poly1
