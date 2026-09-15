package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Int

public data class InheritedOneOfBranchB(
  @param:JsonProperty("bQty")
  @get:JsonProperty("bQty")
  public val bQty: Int? = null,
) : InheritedOneOfParentEffect
