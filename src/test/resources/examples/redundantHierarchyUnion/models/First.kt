package examples.redundantHierarchyUnion.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.Boolean
import kotlin.String

public data class First(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 1)
  override val id: String,
  @param:JsonProperty("description")
  @get:JsonProperty("description")
  override val description: String? = null,
  @param:JsonProperty("enabled")
  @get:JsonProperty("enabled")
  public val enabled: Boolean? = null,
  @get:JsonProperty("kind")
  @get:NotNull
  @param:JsonProperty("kind")
  override val kind: String = "first",
) : Base(id, description)
