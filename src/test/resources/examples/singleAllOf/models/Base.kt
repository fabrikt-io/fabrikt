package examples.singleAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class Base(
  @param:JsonProperty("foo")
  @get:JsonProperty("foo")
  public val foo: String? = null,
)
