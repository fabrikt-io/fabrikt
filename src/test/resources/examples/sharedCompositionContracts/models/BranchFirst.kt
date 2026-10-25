package examples.sharedCompositionContracts.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class BranchFirst(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  override val id: String? = null,
  @param:JsonProperty("custom_field")
  @get:JsonProperty("custom_field")
  override val customField: String? = null,
) : BranchBaseComposite,
    BranchFirstComposite
