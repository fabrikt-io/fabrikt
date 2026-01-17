package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Any

public data class SimpleOneOfs(
  @param:JsonProperty("oneof_property")
  @get:JsonProperty("oneof_property")
  public val oneofProperty: Any? = null,
  @param:JsonProperty("primitive_oneof_property")
  @get:JsonProperty("primitive_oneof_property")
  public val primitiveOneofProperty: Any? = null,
)
