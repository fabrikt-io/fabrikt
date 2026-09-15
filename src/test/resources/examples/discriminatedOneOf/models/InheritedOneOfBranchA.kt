package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Int

public data class InheritedOneOfBranchA(
  @param:JsonProperty("aQty")
  @get:JsonProperty("aQty")
  public val aQty: Int? = null,
) : InheritedOneOfParentEffect
