package examples.allOfParentOrder.models

import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.MutableMap

public data class TypedPlainChild(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  override val id: String,
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  public val name: String? = null,
  @get:JsonProperty("kind")
  @get:NotNull
  @param:JsonProperty("kind")
  override val kind: String = "plainChild",
  @get:JsonIgnore
  override val properties: MutableMap<String, String?> = mutableMapOf(),
) : TypedParent(id, properties = properties)
