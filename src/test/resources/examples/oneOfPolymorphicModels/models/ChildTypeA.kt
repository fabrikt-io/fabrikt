package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ChildTypeA(
  @param:JsonProperty("some_string")
  @get:JsonProperty("some_string")
  @get:NotNull
  public val someString: String,
) : ParentSpec() {
  /**
   * Shows which child type is being returned
   */
  @get:JsonProperty(
    value = "type",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val type: ParentType
    get() = ParentType.CHILD_TYPE_A
}
