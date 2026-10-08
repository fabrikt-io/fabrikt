package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.collections.List

public data class SomeObj(
  @param:JsonProperty("state")
  @get:JsonProperty("state")
  @get:NotNull
  @get:Valid
  public val state: State,
  @param:JsonProperty("arrayOfStates")
  @get:JsonProperty("arrayOfStates")
  public val arrayOfStates: List<@Valid State>? = null,
  @param:JsonProperty("inlinedArray")
  @get:JsonProperty("inlinedArray")
  public val inlinedArray: List<@Valid SomeObjInlinedArray>? = null,
  @param:JsonProperty("inlinedObject")
  @get:JsonProperty("inlinedObject")
  @get:Valid
  public val inlinedObject: SomeObjInlinedObject? = null,
  @param:JsonProperty("inlinedObjectNoMappings")
  @get:JsonProperty("inlinedObjectNoMappings")
  @get:Valid
  public val inlinedObjectNoMappings: SomeObjInlinedObjectNoMappings? = null,
)
