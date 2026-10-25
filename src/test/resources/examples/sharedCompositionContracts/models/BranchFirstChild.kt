package examples.sharedCompositionContracts.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class BranchFirstChild(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  override val id: String? = null,
  @param:JsonProperty("custom_field")
  @get:JsonProperty("custom_field")
  override val customField: String? = null,
  @param:JsonProperty("nested")
  @get:JsonProperty("nested")
  public val nested: String? = null,
) : BranchBaseComposite,
    BranchFirstComposite
