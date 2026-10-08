package examples.discriminatedOneOf.models

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class SomeObj(
  @SerialName("state")
  @get:NotNull
  @get:Valid
  public val state: State,
  @SerialName("arrayOfStates")
  public val arrayOfStates: List<@Valid State>? = null,
  @SerialName("inlinedArray")
  public val inlinedArray: List<@Valid SomeObjInlinedArray>? = null,
  @SerialName("inlinedObject")
  @get:Valid
  public val inlinedObject: SomeObjInlinedObject? = null,
  @SerialName("inlinedObjectNoMappings")
  @get:Valid
  public val inlinedObjectNoMappings: SomeObjInlinedObjectNoMappings? = null,
)
