package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ChildActionA(
  @param:JsonProperty("fieldA")
  @get:JsonProperty("fieldA")
  public val fieldA: String? = null,
) : ParentAction(),
    ChildActionsAll,
    ChildActionsPartial {
  @get:JsonProperty(
    value = "actionType",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val actionType: ParentActionActionType
    get() = ParentActionActionType.CHILD_A
}
