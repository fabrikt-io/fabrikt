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
  @get:Valid
  public val arrayOfStates: List<State>? = null,
  @SerialName("inlinedArray")
  @get:Valid
  public val inlinedArray: List<SomeObjInlinedArray>? = null,
  @SerialName("inlinedObject")
  @get:Valid
  public val inlinedObject: SomeObjInlinedObject? = null,
  @SerialName("inlinedObjectNoMappings")
  @get:Valid
  public val inlinedObjectNoMappings: SomeObjInlinedObjectNoMappings? = null,
)
