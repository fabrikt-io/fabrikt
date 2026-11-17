package examples.allOfParentOrder.models

import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Any
import kotlin.String
import kotlin.collections.MutableMap

public data class IntermediateOrder(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  override val id: String,
  @get:JsonProperty("kind")
  @get:NotNull
  override val kind: String,
  @param:JsonProperty("intermediateProp")
  @get:JsonProperty("intermediateProp")
  public val intermediateProp: String? = null,
  @get:JsonIgnore
  override val properties: MutableMap<String, Any?> = mutableMapOf(),
) : RootOrder(id, properties = properties)
