package examples.discriminatedOneOf.models

import jakarta.validation.Valid
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NestedInlineContainer(
  @SerialName("nested")
  @get:Valid
  public val nested: NestedInlineContainerNested? = null,
  @SerialName("entries")
  public val entries: List<@Valid NestedInlineContainerEntries>? = null,
  @SerialName("recursive")
  @get:Valid
  public val recursive: NestedInlineContainer? = null,
)
