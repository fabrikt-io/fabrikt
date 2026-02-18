package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ChildTypeA(
  @param:JsonProperty("some_string")
  @get:JsonProperty("some_string")
  @get:NotNull
  public val someString: String,
  /**
   * Shows which child type is being returned
   */
  @get:JsonProperty("type")
  @get:NotNull
  @param:JsonProperty("type")
  override val type: ParentType = ParentType.CHILD_TYPE_A,
) : ParentSpec()
