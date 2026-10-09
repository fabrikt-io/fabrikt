package examples.sharedCompositionContracts.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Boolean

public data class AComposite(
  @param:JsonProperty("unrelated")
  @get:JsonProperty("unrelated")
  public val unrelated: Boolean? = null,
)
