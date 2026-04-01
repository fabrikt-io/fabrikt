package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ChildActionA(
  @param:JsonProperty("fieldA")
  @get:JsonProperty("fieldA")
  public val fieldA: String? = null,
  @get:JsonProperty("actionType")
  @get:NotNull
  @param:JsonProperty("actionType")
  override val actionType: ParentActionActionType = ParentActionActionType.CHILD_A,
) : ParentAction()
