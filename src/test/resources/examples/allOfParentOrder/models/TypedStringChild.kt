package examples.allOfParentOrder.models

import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlin.collections.MutableMap

public data class TypedStringChild(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  override val id: String,
  @param:JsonProperty("label")
  @get:JsonProperty("label")
  public val label: String? = null,
  @get:JsonProperty("kind")
  @get:NotNull
  @param:JsonProperty("kind")
  override val kind: String = "stringChild",
  @get:JsonIgnore
  override val properties: MutableMap<String, String?> = mutableMapOf(),
) : TypedParent(id, properties = properties)
