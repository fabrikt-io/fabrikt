package examples.sharedCompositionContracts.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class BranchBase(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  override val id: String? = null,
) : BranchBaseComposite
