package examples.oneOfWithSharedDiscriminatedParent.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int

public data class ChildActionB(
  @param:JsonProperty("fieldB")
  @get:JsonProperty("fieldB")
  public val fieldB: Int? = null,
  @get:JsonProperty("actionType")
  @get:NotNull
  @param:JsonProperty("actionType")
  override val actionType: ParentActionActionType = ParentActionActionType.CHILD_B,
) : ParentAction()
