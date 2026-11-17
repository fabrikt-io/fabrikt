package examples.allOfParentOrder.models

import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Any
import kotlin.Int
import kotlin.String
import kotlin.collections.MutableMap

public data class WidenedIntChild(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  override val id: String,
  @param:JsonProperty("count")
  @get:JsonProperty("count")
  public val count: Int? = null,
  @get:JsonProperty("kind")
  @get:NotNull
  @param:JsonProperty("kind")
  override val kind: String = "intChild",
  @get:JsonIgnore
  override val properties: MutableMap<String, Any?> = mutableMapOf(),
) : WidenedParent(id, properties = properties)
