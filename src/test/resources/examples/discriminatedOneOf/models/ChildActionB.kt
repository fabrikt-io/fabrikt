package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int

public data class ChildActionB(
  @param:JsonProperty("fieldB")
  @get:JsonProperty("fieldB")
  public val fieldB: Int? = null,
) : ParentAction(),
    ChildActionsAll {
  @get:JsonProperty(
    value = "actionType",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val actionType: ParentActionActionType
    get() = ParentActionActionType.CHILD_B
}
