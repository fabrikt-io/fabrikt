package examples.sharedCompositionContracts.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class BranchSecond(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  override val id: String? = null,
  @param:JsonProperty("custom_field")
  @get:JsonProperty("custom_field")
  public val custom_field: String? = null,
  @param:JsonProperty("customField")
  @get:JsonProperty("customField")
  public val customField: String? = null,
) : BranchBaseComposite
