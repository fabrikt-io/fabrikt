package examples.untypedObject.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
public data class Result(
  /**
   * Any data. Object has no schema.
   */
  @SerialName("data")
  public val `data`: JsonObject? = null,
)
