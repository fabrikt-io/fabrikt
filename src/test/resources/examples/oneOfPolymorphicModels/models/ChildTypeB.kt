package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int

public data class ChildTypeB(
  @param:JsonProperty(
    "some_int",
    required = true,
  )
  @get:JsonProperty("some_int")
  @get:NotNull
  public val someInt: Int,
  /**
   * Shows which child type is being returned
   */
  @get:JsonProperty("type")
  @get:NotNull
  @param:JsonProperty("type")
  override val type: ParentType = ParentType.CHILD_TYPE_B,
) : ParentSpec()
