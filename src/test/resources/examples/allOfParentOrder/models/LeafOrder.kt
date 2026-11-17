package examples.allOfParentOrder.models

import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Any
import kotlin.String
import kotlin.collections.MutableMap

public data class LeafOrder(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  override val id: String,
  @param:JsonProperty("intermediateProp")
  @get:JsonProperty("intermediateProp")
  public val intermediateProp: String? = null,
  @param:JsonProperty("leafProp")
  @get:JsonProperty("leafProp")
  public val leafProp: String? = null,
  @get:JsonProperty("kind")
  @get:NotNull
  @param:JsonProperty("kind")
  override val kind: String = "leaf",
  @get:JsonIgnore
  override val properties: MutableMap<String, Any?> = mutableMapOf(),
) : RootOrder(id, properties = properties)
