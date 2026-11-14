package examples.singletonHierarchyUnion.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.String

public data class Middle(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 1)
  override val id: String,
  @param:JsonProperty("middle")
  @get:JsonProperty("middle")
  public val middle: String? = null,
  @get:JsonProperty("kind")
  @get:NotNull
  @param:JsonProperty("kind")
  override val kind: String = "middle",
  @param:JsonProperty("note")
  @get:JsonProperty("note")
  override val note: String? = "hello",
) : Base(id, note)
