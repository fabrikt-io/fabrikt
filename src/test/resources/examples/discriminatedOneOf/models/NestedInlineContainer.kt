package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.collections.List

public data class NestedInlineContainer(
  @param:JsonProperty("nested")
  @get:JsonProperty("nested")
  @get:Valid
  public val nested: NestedInlineContainerNested? = null,
  @param:JsonProperty("entries")
  @get:JsonProperty("entries")
  public val entries: List<@Valid NestedInlineContainerEntries>? = null,
  @param:JsonProperty("recursive")
  @get:JsonProperty("recursive")
  @get:Valid
  public val recursive: NestedInlineContainer? = null,
)
